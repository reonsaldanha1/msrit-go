package edu.msrit.go.data

import android.webkit.JavascriptInterface

class PortalBridge(
    private val onLoginSuccess: (String) -> Unit = {},
    private val onLoginFailure: (String) -> Unit = {},
    private val onVerificationRequired: () -> Unit = {},
    private val onProgressUpdate: (String) -> Unit = {}
) {

    @JavascriptInterface
    fun postMessage(type: String, message: String) {
        when (type) {
            "login_success" -> onLoginSuccess(message)
            "login_failure" -> onLoginFailure(message)
            "verification_required" -> onVerificationRequired()
            "progress" -> onProgressUpdate(message)
        }
    }

    @JavascriptInterface
    fun onDataExtracted(jsonPayload: String) {
        onLoginSuccess(jsonPayload)
    }

    @JavascriptInterface
    fun onError(errorMessage: String) {
        onLoginFailure(errorMessage)
    }

    @JavascriptInterface
    fun onNeedVerification() {
        onVerificationRequired()
    }

    companion object {
        const val PORTAL_URL = "https://parents.msrit.edu/newparents/index.php"
        const val EXAM_URL = "https://exam.msrit.edu/"
        const val OE_URL = "https://msrit-oe.contineo.in:5055/index.php"

        fun getAutoFillScript(usn: String, day: String, month: String, year: String): String {
            val cleanDay = day.trim().padStart(2, '0')
            val cleanMonth = month.trim().padStart(2, '0')
            val cleanYear = year.trim()

            return """
                (function() {
                    try {
                        var uInput = document.getElementById('username');
                        if (uInput) {
                            uInput.value = '$usn';
                            uInput.dispatchEvent(new Event('input', { bubbles: true }));
                            uInput.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                        
                        var dSelect = document.getElementById('dd');
                        if (dSelect) {
                            for (var i = 0; i < dSelect.options.length; i++) {
                                if (dSelect.options[i].value.trim() === '$cleanDay') {
                                    dSelect.selectedIndex = i;
                                    dSelect.dispatchEvent(new Event('change', { bubbles: true }));
                                    break;
                                }
                            }
                        }

                        var mSelect = document.getElementById('mm');
                        if (mSelect) {
                            for (var j = 0; j < mSelect.options.length; j++) {
                                if (mSelect.options[j].value.trim() === '$cleanMonth') {
                                    mSelect.selectedIndex = j;
                                    mSelect.dispatchEvent(new Event('change', { bubbles: true }));
                                    break;
                                }
                            }
                        }

                        var ySelect = document.getElementById('yyyy');
                        if (ySelect) {
                            for (var k = 0; k < ySelect.options.length; k++) {
                                if (ySelect.options[k].value.trim() === '$cleanYear') {
                                    ySelect.selectedIndex = k;
                                    ySelect.dispatchEvent(new Event('change', { bubbles: true }));
                                    break;
                                }
                            }
                            if (typeof putdate === 'function') {
                                putdate();
                            }
                        }
                    } catch (e) {
                        console.error('MSRIT GO Autofill Error:', e);
                    }
                })();
            """.trimIndent()
        }

        fun getAutoSubmitScript(
            usn: String,
            day: String,
            month: String,
            year: String,
            verificationType: String = "Father Mobile Last 4 Digits",
            verificationDigits: String = ""
        ): String {
            val cleanDay = day.trim().padStart(2, '0')
            val cleanMonth = month.trim().padStart(2, '0')
            val cleanYear = year.trim()
            val cleanDigits = verificationDigits.trim()

            return """
                (function() {
                    try {
                        console.log("MSRIT GO Automation Engine initializing...");

                        // Visual progress banner helper inside WebView
                        function showBanner(message, isSuccess) {
                            var bId = 'msrit-go-auto-banner';
                            var banner = document.getElementById(bId);
                            if (!banner) {
                                banner = document.createElement('div');
                                banner.id = bId;
                                banner.style.position = 'fixed';
                                banner.style.top = '10px';
                                banner.style.left = '50%';
                                banner.style.transform = 'translateX(-50%)';
                                banner.style.zIndex = '9999999';
                                banner.style.padding = '8px 16px';
                                banner.style.borderRadius = '20px';
                                banner.style.fontSize = '12px';
                                banner.style.fontWeight = 'bold';
                                banner.style.fontFamily = '-apple-system, BlinkMacSystemFont, sans-serif';
                                banner.style.boxShadow = '0 4px 16px rgba(0,0,0,0.5)';
                                banner.style.transition = 'all 0.3s ease';
                                banner.style.pointerEvents = 'none';
                                document.body.appendChild(banner);
                            }
                            banner.style.backgroundColor = isSuccess ? '#059669' : '#B82226';
                            banner.style.color = '#FFFFFF';
                            banner.innerText = message;
                        }

                        // Prevent multiple conflicting loop instances
                        if (window.__msritEngineInterval) {
                            clearInterval(window.__msritEngineInterval);
                        }

                        function checkAndExecute() {
                            try {
                                var bodyText = document.body ? document.body.innerText : "";

                                // Check for portal error alert
                                var errEl = document.querySelector('.uk-alert-danger') || 
                                            document.querySelector('.alert-error') ||
                                            document.querySelector('.alert-danger');
                                if (errEl && errEl.innerText.trim().length > 0) {
                                    var errMsg = errEl.innerText.trim();
                                    console.error('MSRIT GO Portal Error:', errMsg);
                                    if (window.MsritBridge) {
                                        window.MsritBridge.onError(errMsg);
                                    }
                                    if (window.__msritEngineInterval) clearInterval(window.__msritEngineInterval);
                                    return;
                                }

                                if (bodyText.includes('Invalid Login') || bodyText.includes('Incorrect Password') || bodyText.includes('Authentication Failed')) {
                                    if (window.MsritBridge) {
                                        window.MsritBridge.onError('Invalid USN or Date of Birth credentials.');
                                    }
                                    if (window.__msritEngineInterval) clearInterval(window.__msritEngineInterval);
                                    return;
                                }

                                // -------------------------------------------------------------
                                // STEP 2: 2-Step Verification Screen (Check before Step 1)
                                // -------------------------------------------------------------
                                var isVerificationScreen = bodyText.includes('Select Verification Type') || 
                                                           bodyText.includes('Enter Last 4 Digits') || 
                                                           bodyText.includes('Last 4 digits of the selected ID') ||
                                                           (document.querySelector('select') && (document.body.innerHTML.toLowerCase().includes('father mobile') || document.body.innerHTML.toLowerCase().includes('mother mobile') || document.body.innerHTML.toLowerCase().includes('abc id')));

                                if (isVerificationScreen && !window.__msritStep2Done) {
                                    window.__msritStep2Done = true;
                                    console.log('MSRIT GO: Detected 2-Step Verification Screen!');
                                    showBanner('MSRIT GO: Entering verification details...', false);

                                    if (window.MsritBridge) {
                                        window.MsritBridge.postMessage('progress', 'Verifying 2-Step challenge ($verificationType)...');
                                    }

                                    // 1. Select Verification Type in dropdown / select
                                    var targetType = '$verificationType'.toLowerCase();
                                    var selects = document.querySelectorAll('select');
                                    var vSelect = null;
                                    for (var s = 0; s < selects.length; s++) {
                                        var opts = selects[s].options;
                                        for (var o = 0; o < opts.length; o++) {
                                            var oText = opts[o].text.toLowerCase();
                                            if (oText.includes('father') || oText.includes('mother') || oText.includes('abc') || oText.includes('option')) {
                                                vSelect = selects[s];
                                                break;
                                            }
                                        }
                                        if (vSelect) break;
                                    }

                                    if (vSelect) {
                                        var matchedIdx = -1;
                                        for (var k = 0; k < vSelect.options.length; k++) {
                                            var txt = vSelect.options[k].text.toLowerCase();
                                            if (targetType.includes('father') && txt.includes('father')) { matchedIdx = k; break; }
                                            else if (targetType.includes('mother') && txt.includes('mother')) { matchedIdx = k; break; }
                                            else if (targetType.includes('abc') && (txt.includes('abc') || txt.includes('id'))) { matchedIdx = k; break; }
                                        }

                                        if (matchedIdx >= 0) {
                                            vSelect.selectedIndex = matchedIdx;
                                            vSelect.value = vSelect.options[matchedIdx].value;
                                            vSelect.dispatchEvent(new Event('change', { bubbles: true }));
                                            vSelect.dispatchEvent(new Event('input', { bubbles: true }));
                                            
                                            // Handle UIKit styled select display text
                                            var parentSelect = vSelect.closest('.uk-form-select') || vSelect.parentElement;
                                            if (parentSelect) {
                                                var labelEl = parentSelect.querySelector('span, a');
                                                if (labelEl) labelEl.innerText = vSelect.options[matchedIdx].text;
                                            }
                                            console.log('MSRIT GO: Selected verification type:', vSelect.options[matchedIdx].text);
                                        }
                                    }

                                    // Also handle custom dropdowns or radio lists
                                    var radios = document.querySelectorAll('input[type="radio"], .uk-radio');
                                    radios.forEach(function(radio) {
                                        var parent = radio.closest('label') || radio.parentElement;
                                        var rText = parent ? parent.innerText.toLowerCase() : "";
                                        if ((targetType.includes('father') && rText.includes('father')) ||
                                            (targetType.includes('mother') && rText.includes('mother')) ||
                                            (targetType.includes('abc') && (rText.includes('abc') || rText.includes('id')))) {
                                            radio.checked = true;
                                            radio.click();
                                            radio.dispatchEvent(new Event('change', { bubbles: true }));
                                        }
                                    });

                                    // 2. Fill the 4 digits into the 4 boxes or single input
                                    var digits = '$cleanDigits';
                                    if (digits.length >= 4) {
                                        var allInputs = Array.from(document.querySelectorAll('input:not([type="hidden"]):not([type="submit"]):not([type="button"]):not([type="radio"])'))
                                            .filter(function(i) {
                                                return i.id !== 'username' && i.id !== 'dd' && i.id !== 'mm' && i.id !== 'yyyy';
                                            });

                                        console.log('MSRIT GO: Found', allInputs.length, 'digit input boxes');

                                        if (allInputs.length >= 4) {
                                            for (var d = 0; d < 4; d++) {
                                                var ch = digits.charAt(d);
                                                allInputs[d].value = ch;
                                                allInputs[d].dispatchEvent(new Event('input', { bubbles: true }));
                                                allInputs[d].dispatchEvent(new Event('change', { bubbles: true }));
                                                allInputs[d].dispatchEvent(new KeyboardEvent('keydown', { key: ch, bubbles: true }));
                                                allInputs[d].dispatchEvent(new KeyboardEvent('keypress', { key: ch, bubbles: true }));
                                                allInputs[d].dispatchEvent(new KeyboardEvent('keyup', { key: ch, bubbles: true }));
                                            }
                                        } else if (allInputs.length > 0) {
                                            allInputs[0].value = digits;
                                            allInputs[0].dispatchEvent(new Event('input', { bubbles: true }));
                                            allInputs[0].dispatchEvent(new Event('change', { bubbles: true }));
                                            allInputs[0].dispatchEvent(new KeyboardEvent('keyup', { key: digits.charAt(3), bubbles: true }));
                                        }
                                    }

                                    // Explicit 2-second pause before clicking verification submit
                                    showBanner('MSRIT GO: Verification PIN entered. Logging in in 2s...', false);
                                    if (window.MsritBridge) {
                                        window.MsritBridge.postMessage('progress', 'Verification PIN entered. Waiting 2 seconds...');
                                    }

                                    setTimeout(function() {
                                        var submitBtn = document.querySelector('button[type="submit"], input[type="submit"], .cn-submit, .uk-button-primary');
                                        if (!submitBtn) {
                                            var buttons = document.querySelectorAll('button, a.uk-button, input[type="button"]');
                                            for (var b = 0; b < buttons.length; b++) {
                                                var bTxt = buttons[b].innerText.toLowerCase();
                                                if (bTxt.includes('submit') || bTxt.includes('verify') || bTxt.includes('login') || bTxt.includes('proceed') || bTxt.includes('continue')) {
                                                    submitBtn = buttons[b];
                                                    break;
                                                }
                                            }
                                        }

                                        if (submitBtn) {
                                            console.log('MSRIT GO: Auto-clicking verification submit button after 2s');
                                            submitBtn.click();
                                        } else {
                                            var form = document.querySelector('form');
                                            if (form) form.submit();
                                        }
                                    }, 2000);

                                    return;
                                }

                                // -------------------------------------------------------------
                                // STEP 1: Initial Login Page (USN + DOB)
                                // -------------------------------------------------------------
                                var uInput = document.getElementById('username');
                                var dSelect = document.getElementById('dd');
                                var mSelect = document.getElementById('mm');
                                var ySelect = document.getElementById('yyyy');

                                if (uInput && dSelect && !window.__msritStep1Done) {
                                    window.__msritStep1Done = true;
                                    console.log('MSRIT GO: Detected Initial Login Page, autofilling credentials...');
                                    showBanner('MSRIT GO: Entering credentials...', false);

                                    if (window.MsritBridge) {
                                        window.MsritBridge.postMessage('progress', 'Entering USN & Date of Birth...');
                                    }

                                    // Enter USN
                                    uInput.value = '$usn';
                                    uInput.dispatchEvent(new Event('input', { bubbles: true }));
                                    uInput.dispatchEvent(new Event('change', { bubbles: true }));
                                    
                                    // Enter Day
                                    for (var i = 0; i < dSelect.options.length; i++) {
                                        if (dSelect.options[i].value.trim() === '$cleanDay') {
                                            dSelect.selectedIndex = i;
                                            dSelect.value = dSelect.options[i].value;
                                            dSelect.dispatchEvent(new Event('change', { bubbles: true }));
                                            break;
                                        }
                                    }

                                    // Enter Month
                                    if (mSelect) {
                                        for (var j = 0; j < mSelect.options.length; j++) {
                                            if (mSelect.options[j].value.trim() === '$cleanMonth') {
                                                mSelect.selectedIndex = j;
                                                mSelect.value = mSelect.options[j].value;
                                                mSelect.dispatchEvent(new Event('change', { bubbles: true }));
                                                break;
                                            }
                                        }
                                    }

                                    // Enter Year
                                    if (ySelect) {
                                        for (var k = 0; k < ySelect.options.length; k++) {
                                            if (ySelect.options[k].value.trim() === '$cleanYear') {
                                                ySelect.selectedIndex = k;
                                                ySelect.value = ySelect.options[k].value;
                                                ySelect.dispatchEvent(new Event('change', { bubbles: true }));
                                                break;
                                            }
                                        }
                                    }

                                    // Set hidden passwd directly and invoke Contineo putdate()
                                    var passwdInput = document.getElementById('passwd');
                                    if (passwdInput) {
                                        passwdInput.value = '$cleanYear-$cleanMonth-$cleanDay';
                                    }
                                    if (typeof putdate === 'function') {
                                        putdate();
                                    }

                                    // Explicit 2-second pause before submitting initial login form
                                    showBanner('MSRIT GO: Credentials entered. Waiting 2s before login...', false);
                                    if (window.MsritBridge) {
                                        window.MsritBridge.postMessage('progress', 'Credentials entered. Waiting 2 seconds...');
                                    }

                                    setTimeout(function() {
                                        var loginBtn = document.querySelector('.cn-login-btn') || 
                                                       document.querySelector('input[type="submit"]') ||
                                                       document.querySelector('#login-form button[type="submit"]') ||
                                                       document.querySelector('#login-form input[type="submit"]');

                                        if (loginBtn) {
                                            console.log('MSRIT GO: Submitting initial login form after 2s');
                                            loginBtn.click();
                                        } else {
                                            var form = document.getElementById('login-form') || document.querySelector('form.cn-landing-login');
                                            if (form) form.submit();
                                        }
                                    }, 2000);

                                    return;
                                }

                                // -------------------------------------------------------------
                                // STEP 3: Authenticated Dashboard / Records Screen
                                // -------------------------------------------------------------
                                var isAuthenticated = (bodyText.includes('Logout') || 
                                                       bodyText.includes('Student Name') || 
                                                       bodyText.includes('Attendance') || 
                                                       bodyText.includes('CIE') || 
                                                       bodyText.includes('Welcome') || 
                                                       document.querySelector('.uk-navbar, .cn-header, .cn-profile')) &&
                                                       !uInput && !isVerificationScreen;

                                if (isAuthenticated && !window.__msritStep3Done) {
                                    window.__msritStep3Done = true;
                                    if (window.__msritEngineInterval) clearInterval(window.__msritEngineInterval);

                                    console.log('MSRIT GO: Authenticated page confirmed! Waiting 2s before extracting data...');
                                    showBanner('MSRIT GO: Portal Connected! Extracting records in 2s...', true);

                                    if (window.MsritBridge) {
                                        window.MsritBridge.postMessage('progress', 'Connected to Portal! Waiting 2 seconds to extract records...');
                                    }

                                    // Explicit 2-second pause after page load before running scraper
                                    setTimeout(function() {
                                        showBanner('MSRIT GO: Syncing attendance & CIE marks...', true);
                                        ${getScraperScript()}
                                    }, 2000);
                                }

                            } catch (err) {
                                console.error('MSRIT GO AutoSubmit error in checkAndExecute:', err);
                            }
                        }

                        // Run immediate check and install interval watcher
                        checkAndExecute();
                        window.__msritEngineInterval = setInterval(checkAndExecute, 400);

                    } catch (e) {
                        console.error('MSRIT GO AutoSubmit Error:', e);
                        if (window.MsritBridge) {
                            window.MsritBridge.onError('Automation notice: ' + e);
                        }
                    }
                })();
            """.trimIndent()
        }

        fun getScraperScript(): String {
            return """
                (function() {
                    function runExtraction(attemptCount) {
                        attemptCount = attemptCount || 0;
                        try {
                            console.log("MSRIT GO: Running portal DOM extractor (attempt " + attemptCount + ")...");
                            var bodyText = document.body ? document.body.innerText : "";

                            // Ensure not on login or verification screen
                            var hasLoginForm = document.getElementById('username') !== null && 
                                               document.getElementById('dd') !== null;
                            var isVerificationScreen = bodyText.includes('Select Verification Type') || 
                                                       bodyText.includes('Enter Last 4 Digits');

                            if (hasLoginForm || isVerificationScreen) {
                                console.log("MSRIT GO: Still in auth phase, extraction deferred.");
                                return;
                            }

                            // Gather all accessible documents (main document + any iframes)
                            var docList = [document];
                            try {
                                var iframes = document.querySelectorAll('iframe');
                                for (var f = 0; f < iframes.length; f++) {
                                    if (iframes[f].contentDocument) {
                                        docList.push(iframes[f].contentDocument);
                                    }
                                }
                            } catch (e) {
                                console.log("MSRIT GO: Frame inspection notice: " + e);
                            }

                            // --- 1. Extract Student Profile ---
                            var detectedUsn = "";
                            var usnMatch = bodyText.match(/\b1MS\d{2}[A-Z]{2,4}\d{2,4}(?:-[A-Z0-9]+)?\b/i);
                            if (usnMatch) {
                                detectedUsn = usnMatch[0].toUpperCase();
                            }

                            var detectedName = "";
                            var nameMatch = bodyText.match(/(?:Student Name|Name|Candidate)\s*[:\-]\s*([A-Za-z\s.]+)/i);
                            if (nameMatch && nameMatch[1]) {
                                detectedName = nameMatch[1].trim().replace(/\s{2,}/g, ' ');
                            } else {
                                var heading = document.querySelector('.uk-article-title, .cn-student-name, h2, h3, h4');
                                if (heading && heading.innerText.trim().length > 3 && !heading.innerText.toLowerCase().includes('welcome')) {
                                    detectedName = heading.innerText.trim();
                                }
                            }
                            if (!detectedName || detectedName.length < 2 || detectedName.toLowerCase().includes('welcome') || detectedName.toLowerCase().includes('ramaiah')) {
                                detectedName = detectedUsn.length > 0 ? "MSRIT Student (" + detectedUsn + ")" : "MSRIT Student";
                            }

                            var detectedSem = 5;
                            var semMatch = bodyText.match(/(\d)(?:st|nd|rd|th)?\s*Sem(?:ester)?/i);
                            if (semMatch) {
                                detectedSem = parseInt(semMatch[1]);
                            }

                            var detectedBranch = "Computer Science & Engineering";
                            var branchMatch = bodyText.match(/(?:Department|Branch|Programme|Course)\s*[:\-]\s*([A-Za-z\s&()]+)/i);
                            if (branchMatch && branchMatch[1].trim().length > 4) {
                                detectedBranch = branchMatch[1].trim().split('\n')[0].trim();
                            } else if (detectedUsn.includes("CI")) {
                                detectedBranch = "Computer Science & Engineering (Cyber Security)";
                            } else if (detectedUsn.includes("CS")) {
                                detectedBranch = "Computer Science & Engineering";
                            } else if (detectedUsn.includes("IS")) {
                                detectedBranch = "Information Science & Engineering";
                            } else if (detectedUsn.includes("EC")) {
                                detectedBranch = "Electronics & Communication Engg";
                            } else if (detectedUsn.includes("EE")) {
                                detectedBranch = "Electrical & Electronics Engg";
                            } else if (detectedUsn.includes("AI") || detectedUsn.includes("AD")) {
                                detectedBranch = "Artificial Intelligence & Data Science";
                            } else if (detectedUsn.includes("ME")) {
                                detectedBranch = "Mechanical Engineering";
                            } else if (detectedUsn.includes("CV")) {
                                detectedBranch = "Civil Engineering";
                            } else if (detectedUsn.includes("BT")) {
                                detectedBranch = "Biotechnology";
                            }

                            var detectedSec = "A";
                            var secMatch = bodyText.match(/Sec(?:tion)?\s*[:\-]\s*([A-Z])/i);
                            if (secMatch) detectedSec = secMatch[1].toUpperCase();

                            var detectedProctor = "Department Faculty Mentor";
                            var proctorMatch = bodyText.match(/(?:Proctor|Counselor|Mentor)\s*(?:Name)?\s*[:\-]\s*([A-Za-z\s.]+)/i);
                            if (proctorMatch) detectedProctor = proctorMatch[1].trim().split('\n')[0].trim();

                            // --- 2. Advanced Multi-strategy Table Parsing ---
                            var subjectMap = {}; // Key: code.toUpperCase() -> { attendance: {}, marks: {}, sessions: [] }

                            docList.forEach(function(doc) {
                                var tables = doc.querySelectorAll('table');
                                tables.forEach(function(table) {
                                    var rows = Array.from(table.querySelectorAll('tr'));
                                    if (rows.length < 2) return;

                                    // Build unified header mapping by scanning the first 2-3 rows
                                    var headerTexts = [];
                                    var headerRowLimit = Math.min(3, rows.length);
                                    var maxCols = 0;

                                    for (var r = 0; r < headerRowLimit; r++) {
                                        var cells = Array.from(rows[r].querySelectorAll('th, td'));
                                        maxCols = Math.max(maxCols, cells.length);
                                    }

                                    for (var c = 0; c < maxCols; c++) {
                                        headerTexts[c] = "";
                                    }

                                    for (var hr = 0; hr < headerRowLimit; hr++) {
                                        var hCells = Array.from(rows[hr].querySelectorAll('th, td'));
                                        var colPtr = 0;
                                        hCells.forEach(function(cell) {
                                            var colspan = parseInt(cell.getAttribute('colspan')) || 1;
                                            var text = cell.innerText.trim().toLowerCase();
                                            for (var span = 0; span < colspan; span++) {
                                                if (colPtr < maxCols) {
                                                    headerTexts[colPtr] = (headerTexts[colPtr] + " " + text).trim();
                                                    colPtr++;
                                                }
                                            }
                                        });
                                    }

                                    // Identify column roles
                                    var colRoles = {
                                        code: -1,
                                        title: -1,
                                        credits: -1,
                                        held: -1,
                                        attended: -1,
                                        pct: -1,
                                        cie1: -1,
                                        cie2: -1,
                                        cie3: -1,
                                        quiz: -1,
                                        assign: -1,
                                        lab: -1,
                                        totalCie: -1,
                                        action: -1
                                    };

                                    headerTexts.forEach(function(ht, idx) {
                                        if (colRoles.code === -1 && (ht.includes('code') || ht.includes('course id') || ht.includes('sub code') || ht.includes('subject code'))) {
                                            colRoles.code = idx;
                                        } else if (colRoles.title === -1 && (ht.includes('title') || ht.includes('course name') || ht.includes('subject name') || ht.includes('description') || ht.includes('subject') || ht.includes('course'))) {
                                            colRoles.title = idx;
                                        } else if (colRoles.credits === -1 && ht.includes('credit')) {
                                            colRoles.credits = idx;
                                        } else if (colRoles.held === -1 && (ht.includes('held') || ht.includes('conducted') || ht.includes('total classes') || ht.includes('classes held') || ht.includes('total hours'))) {
                                            colRoles.held = idx;
                                        } else if (colRoles.attended === -1 && (ht.includes('attended') || ht.includes('present') || ht.includes('classes attended') || ht.includes('hours attended'))) {
                                            colRoles.attended = idx;
                                        } else if (colRoles.pct === -1 && (ht.includes('%') || ht.includes('percentage') || ht.includes('att %'))) {
                                            colRoles.pct = idx;
                                        } else if (colRoles.cie1 === -1 && (ht.includes('cie 1') || ht.includes('cie-1') || ht.includes('cie1') || ht.includes('ia 1') || ht.includes('ia-1') || ht.includes('test 1') || ht.includes('test-1') || ht.includes('t1') || ht.includes('internal 1'))) {
                                            colRoles.cie1 = idx;
                                        } else if (colRoles.cie2 === -1 && (ht.includes('cie 2') || ht.includes('cie-2') || ht.includes('cie2') || ht.includes('ia 2') || ht.includes('ia-2') || ht.includes('test 2') || ht.includes('test-2') || ht.includes('t2') || ht.includes('internal 2'))) {
                                            colRoles.cie2 = idx;
                                        } else if (colRoles.cie3 === -1 && (ht.includes('cie 3') || ht.includes('cie-3') || ht.includes('cie3') || ht.includes('ia 3') || ht.includes('ia-3') || ht.includes('test 3') || ht.includes('test-3') || ht.includes('t3') || ht.includes('internal 3'))) {
                                            colRoles.cie3 = idx;
                                        } else if (colRoles.quiz === -1 && (ht.includes('quiz') || ht.includes('q1') || ht.includes('q2') || ht.includes('online test'))) {
                                            colRoles.quiz = idx;
                                        } else if (colRoles.assign === -1 && (ht.includes('assign') || ht.includes('aat') || ht.includes('activity') || ht.includes('self study'))) {
                                            colRoles.assign = idx;
                                        } else if (colRoles.lab === -1 && (ht.includes('lab') || ht.includes('practical') || ht.includes('record'))) {
                                            colRoles.lab = idx;
                                        } else if (colRoles.totalCie === -1 && (ht.includes('total cie') || ht.includes('final cie') || ht.includes('total ia') || ht.includes('cie total') || ht.includes('cie marks') || ht.includes('total internal') || ht.includes('cie avg') || ht.includes('total (50)') || (ht.includes('total') && !ht.includes('class') && !ht.includes('hour')))) {
                                            colRoles.totalCie = idx;
                                        } else if (colRoles.action === -1 && (ht.includes('view') || ht.includes('detail') || ht.includes('action'))) {
                                            colRoles.action = idx;
                                        }
                                    });

                                    // Iterate data rows (skip headers)
                                    for (var rIdx = 1; rIdx < rows.length; rIdx++) {
                                        var row = rows[rIdx];
                                        var cells = Array.from(row.querySelectorAll('td'));
                                        if (cells.length < 3) continue;

                                        var rowText = row.innerText.trim();
                                        if (rowText.toLowerCase().includes('total') && cells.length < 5) continue;

                                        // 1. Find Course Code
                                        var code = "";
                                        if (colRoles.code >= 0 && cells[colRoles.code]) {
                                            code = cells[colRoles.code].innerText.trim();
                                        }
                                        if (!code || !/^[A-Z0-9-]{3,12}$/i.test(code)) {
                                            for (var ci = 0; ci < cells.length; ci++) {
                                                var val = cells[ci].innerText.trim();
                                                var cMatch = val.match(/\b([1-2][0-9][A-Z]{2,4}[0-9]{2,3}[A-Z]?|[A-Z]{2,4}[0-9]{2,4}[A-Z]?)\b/i);
                                                if (cMatch) {
                                                    code = cMatch[1];
                                                    break;
                                                }
                                            }
                                        }

                                        if (!code || code.length < 3) continue;
                                        code = code.toUpperCase();

                                        // 2. Find Course Title
                                        var title = "";
                                        if (colRoles.title >= 0 && cells[colRoles.title]) {
                                            title = cells[colRoles.title].innerText.trim();
                                        }
                                        if (!title || title.length < 3 || title === code) {
                                            for (var ti = 0; ti < cells.length; ti++) {
                                                var tVal = cells[ti].innerText.trim();
                                                if (tVal.length > 4 && isNaN(tVal) && !tVal.includes('%') && tVal !== code && !/^[A-Z0-9-]{3,10}$/i.test(tVal)) {
                                                    title = tVal;
                                                    break;
                                                }
                                            }
                                        }
                                        if (!title) title = code;

                                        // Clean title
                                        title = title.replace(/\s+/g, ' ').trim();

                                        // Credits
                                        var credits = 4;
                                        if (colRoles.credits >= 0 && cells[colRoles.credits]) {
                                            var cr = parseInt(cells[colRoles.credits].innerText.trim());
                                            if (!isNaN(cr) && cr > 0 && cr <= 10) credits = cr;
                                        }

                                        if (!subjectMap[code]) {
                                            subjectMap[code] = {
                                                code: code,
                                                title: title,
                                                credits: credits,
                                                faculty: "Dept Faculty",
                                                type: code.toLowerCase().includes('l') ? "Practical" : "Theory",
                                                attended: null,
                                                total: null,
                                                sessions: [],
                                                cie1: null,
                                                cie2: null,
                                                cie3: null,
                                                assignment: null,
                                                quiz: null,
                                                labInternal: null,
                                                totalInternal: null
                                            };
                                        }

                                        var sub = subjectMap[code];
                                        if (title.length > sub.title.length) sub.title = title;

                                        // --- Parse Attendance ---
                                        var parsedAttended = null;
                                        var parsedHeld = null;

                                        for (var ai = 0; ai < cells.length; ai++) {
                                            var cText = cells[ai].innerText.trim();
                                            var slashMatch = cText.match(/(\d+)\s*\/\s*(\d+)/);
                                            if (slashMatch) {
                                                parsedAttended = parseInt(slashMatch[1]);
                                                parsedHeld = parseInt(slashMatch[2]);
                                                break;
                                            }
                                        }

                                        if (parsedAttended === null) {
                                            if (colRoles.attended >= 0 && cells[colRoles.attended]) {
                                                var attVal = parseInt(cells[colRoles.attended].innerText.trim());
                                                if (!isNaN(attVal)) parsedAttended = attVal;
                                            }
                                            if (colRoles.held >= 0 && cells[colRoles.held]) {
                                                var heldVal = parseInt(cells[colRoles.held].innerText.trim());
                                                if (!isNaN(heldVal)) parsedHeld = heldVal;
                                            }
                                        }

                                        if (parsedAttended === null || parsedHeld === null) {
                                            var numCells = [];
                                            cells.forEach(function(c, idx) {
                                                var v = parseInt(c.innerText.trim());
                                                if (!isNaN(v) && v >= 0 && v <= 150 && idx !== colRoles.code) {
                                                    numCells.push(v);
                                                }
                                            });

                                            if (numCells.length >= 2) {
                                                var pctVal = null;
                                                if (colRoles.pct >= 0 && cells[colRoles.pct]) {
                                                    var p = parseFloat(cells[colRoles.pct].innerText.replace('%', '').trim());
                                                    if (!isNaN(p)) pctVal = p;
                                                }

                                                for (var n1 = 0; n1 < numCells.length; n1++) {
                                                    for (var n2 = 0; n2 < numCells.length; n2++) {
                                                        if (n1 !== n2 && numCells[n1] <= numCells[n2] && numCells[n2] > 0) {
                                                            var calcPct = (numCells[n1] / numCells[n2]) * 100.0;
                                                            if (pctVal !== null && Math.abs(calcPct - pctVal) < 2.0) {
                                                                parsedAttended = numCells[n1];
                                                                parsedHeld = numCells[n2];
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (parsedAttended !== null) break;
                                                }
                                            }
                                        }

                                        if (parsedAttended !== null && parsedHeld !== null) {
                                            if (parsedAttended > parsedHeld && parsedHeld > 0) {
                                                var temp = parsedAttended;
                                                parsedAttended = parsedHeld;
                                                parsedHeld = temp;
                                            }
                                            sub.attended = parsedAttended;
                                            sub.total = parsedHeld;
                                        }

                                        // --- Parse Date-wise Attendance Sessions from Button / Popup ---
                                        var parsedSessions = [];
                                        var viewBtns = row.querySelectorAll('a[onclick*="popUp"], button[onclick*="popUp"], a.uk-button, button.uk-button, a[data-uk-modal], a[href*="popUp"], .uk-icon-button');

                                        viewBtns.forEach(function(btn) {
                                            var onclickStr = btn.getAttribute('onclick') || btn.getAttribute('href') || "";
                                            // Check popUp('...')
                                            var popMatch = onclickStr.match(/popUp\s*\(\s*['"](.*?)['"]\s*\)/);
                                            if (popMatch && popMatch[1]) {
                                                var rawData = popMatch[1];
                                                var chunks = rawData.split('|||');
                                                chunks.forEach(function(chunk) {
                                                    if (chunk && chunk.trim().length > 3) {
                                                        var parts = chunk.split(/[:,-]/);
                                                        var dStr = parts[0] ? parts[0].trim() : "Session";
                                                        var isP = chunk.toLowerCase().includes('present') || chunk.toLowerCase().includes('p');
                                                        parsedSessions.push({
                                                            date: dStr,
                                                            timeOrSlot: parts[1] ? parts[1].trim() : "Regular Class",
                                                            isPresent: isP,
                                                            topicOrRemark: parts[2] ? parts[2].trim() : ""
                                                        });
                                                    }
                                                });
                                            }
                                        });

                                        // Check if #due2 or #atPopup is present in DOM with table
                                        var atPopup = doc.getElementById('atPopup') || doc.getElementById('due2');
                                        if (atPopup && atPopup.innerHTML.length > 20) {
                                            var popRows = atPopup.querySelectorAll('tr');
                                            popRows.forEach(function(pr) {
                                                var prCells = pr.querySelectorAll('td');
                                                if (prCells.length >= 2) {
                                                    var pDate = prCells[0].innerText.trim();
                                                    var pStatus = prCells[prCells.length - 1].innerText.trim().toLowerCase();
                                                    var isP = pStatus.includes('present') || pStatus === 'p';
                                                    if (/\d/.test(pDate)) {
                                                        parsedSessions.push({
                                                            date: pDate,
                                                            timeOrSlot: prCells.length > 2 ? prCells[1].innerText.trim() : "Slot 1",
                                                            isPresent: isP,
                                                            topicOrRemark: ""
                                                        });
                                                    }
                                                }
                                            });
                                        }

                                        if (parsedSessions.length > 0 && sub.sessions.length === 0) {
                                            sub.sessions = parsedSessions;
                                        }

                                        // --- Parse CIE Marks ---
                                        function parseCellFloat(cellIdx) {
                                            if (cellIdx >= 0 && cells[cellIdx]) {
                                                var val = parseFloat(cells[cellIdx].innerText.trim());
                                                if (!isNaN(val) && val >= 0) return val;
                                            }
                                            return null;
                                        }

                                        var c1 = parseCellFloat(colRoles.cie1);
                                        var c2 = parseCellFloat(colRoles.cie2);
                                        var c3 = parseCellFloat(colRoles.cie3);
                                        var q = parseCellFloat(colRoles.quiz);
                                        var a = parseCellFloat(colRoles.assign);
                                        var l = parseCellFloat(colRoles.lab);
                                        var tot = parseCellFloat(colRoles.totalCie);

                                        if (c1 !== null) sub.cie1 = c1;
                                        if (c2 !== null) sub.cie2 = c2;
                                        if (c3 !== null) sub.cie3 = c3;
                                        if (q !== null) sub.quiz = q;
                                        if (a !== null) sub.assignment = a;
                                        if (l !== null) sub.labInternal = l;
                                        if (tot !== null) sub.totalInternal = tot;
                                    }
                                });
                            });

                            // Transform subjectMap into final attendance & CIE marks lists
                            var attendanceList = [];
                            var cieMarksList = [];

                            Object.keys(subjectMap).forEach(function(code) {
                                var s = subjectMap[code];

                                if (s.total !== null && s.attended !== null) {
                                    attendanceList.push({
                                        code: s.code,
                                        title: s.title,
                                        attended: s.attended,
                                        total: s.total > 0 ? s.total : s.attended,
                                        credits: s.credits,
                                        faculty: s.faculty,
                                        type: s.type,
                                        sessions: s.sessions || []
                                    });
                                }

                                var hasMarks = s.cie1 !== null || s.cie2 !== null || s.cie3 !== null || 
                                               s.assignment !== null || s.quiz !== null || s.totalInternal !== null;

                                if (hasMarks || s.total !== null) {
                                    var calcTotal = s.totalInternal;
                                    if (calcTotal === null) {
                                        var testSum = 0.0;
                                        var testCount = 0;
                                        if (s.cie1 !== null) { testSum += s.cie1; testCount++; }
                                        if (s.cie2 !== null) { testSum += s.cie2; testCount++; }
                                        if (s.cie3 !== null) { testSum += s.cie3; testCount++; }
                                        var testAvg = testCount > 0 ? (testSum / testCount) : 0.0;
                                        calcTotal = testAvg + (s.assignment || 0.0) + (s.quiz || 0.0) + (s.labInternal || 0.0);
                                    }

                                    cieMarksList.push({
                                        code: s.code,
                                        title: s.title,
                                        credits: s.credits,
                                        cie1: s.cie1,
                                        cie2: s.cie2,
                                        cie3: s.cie3,
                                        assignment: s.assignment,
                                        quiz: s.quiz,
                                        labInternal: s.labInternal,
                                        totalInternal: Math.min(50.0, Math.round(calcTotal * 10) / 10),
                                        maxInternal: 50.0
                                    });
                                }
                            });

                            // --- 3. Extract College Notices / Circulars ---
                            var circulars = [];
                            var noticeNodes = document.querySelectorAll('.cn-events li, .cn-alert-circulars a, ul.uk-list li, .notice-board li');
                            noticeNodes.forEach(function(node, idx) {
                                var text = node.innerText.trim();
                                var a = node.querySelector('a');
                                var url = a ? a.href : "https://parents.msrit.edu/newparents/index.php";
                                if (text.length > 8 && !text.toLowerCase().includes('copyright')) {
                                    circulars.push({
                                        id: "p_notice_" + idx,
                                        title: text.replace(/Click here/gi, '').trim(),
                                        date: "Portal Notice",
                                        category: text.toLowerCase().includes('result') ? "Exam" : "Notice",
                                        linkUrl: url,
                                        isPdf: url.toLowerCase().endsWith('.pdf'),
                                        isUrgent: text.toLowerCase().includes('mandatory') || text.toLowerCase().includes('important')
                                    });
                                }
                            });

                            // Retry if tables not yet populated
                            if (attendanceList.length === 0 && attemptCount < 4) {
                                console.log("MSRIT GO: Attendance tables not yet populated, retrying " + (attemptCount + 1));
                                var navLinks = document.querySelectorAll('a, button, .uk-tab a');
                                for (var nl = 0; nl < navLinks.length; nl++) {
                                    var nlTxt = navLinks[nl].innerText.toLowerCase();
                                    if (nlTxt.includes('attendance') || nlTxt.includes('cie') || nlTxt.includes('marks') || nlTxt.includes('academic')) {
                                        navLinks[nl].click();
                                        break;
                                    }
                                }

                                setTimeout(function() {
                                    runExtraction(attemptCount + 1);
                                }, 1000);
                                return;
                            }

                            var payload = {
                                success: true,
                                isLoggedIn: true,
                                profile: {
                                    usn: detectedUsn,
                                    name: detectedName,
                                    department: detectedBranch,
                                    semester: detectedSem,
                                    section: detectedSec,
                                    cycle: "Higher Semester (UG)",
                                    academicYear: "2026 - 2027",
                                    proctorName: detectedProctor,
                                    proctorEmail: "proctor@msrit.edu",
                                    proctorCabin: "Apex Block"
                                },
                                attendance: attendanceList,
                                marks: cieMarksList,
                                circulars: circulars
                            };

                            console.log("MSRIT GO: Extraction successful!", payload);

                            if (window.MsritBridge) {
                                window.MsritBridge.onDataExtracted(JSON.stringify(payload));
                            }
                        } catch (err) {
                            console.error("MSRIT GO Extraction Exception:", err);
                            if (window.MsritBridge) {
                                window.MsritBridge.onError("Extraction notice: " + err);
                            }
                        }
                    }

                    // Start extraction
                    runExtraction(0);
                })();
            """.trimIndent()
        }

        fun getStyleEnhancementScript(): String {
            return """
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = `
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important;
                            background-color: #0F1117 !important;
                            color: #F1F5F9 !important;
                        }
                    `;
                    document.head.appendChild(style);
                })();
            """.trimIndent()
        }
    }
}
