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
                        var bodyText = document.body ? document.body.innerText : "";

                        // Check for error alert
                        var errEl = document.querySelector('.uk-alert-danger') || document.querySelector('.alert-error');
                        if (errEl && errEl.innerText.trim().length > 0) {
                            var errMsg = errEl.innerText.trim();
                            console.error('MSRIT GO Portal Error:', errMsg);
                            if (window.MsritBridge) {
                                window.MsritBridge.onError(errMsg);
                            }
                            return;
                        }

                        // CASE 1: 2-Step Verification Screen ("Select Verification Type" & "Enter Last 4 Digits")
                        var isVerificationScreen = bodyText.includes('Select Verification Type') || 
                                                   bodyText.includes('Enter Last 4 Digits') || 
                                                   bodyText.includes('Last 4 digits of the selected ID');

                        if (isVerificationScreen) {
                            console.log('MSRIT GO: Detected 2-Step Verification Screen!');
                            if (window.MsritBridge) {
                                window.MsritBridge.postMessage('progress', 'Completing 2-Step Verification ($verificationType)...');
                            }

                            // 1. Select Verification Type in dropdown / select
                            var targetType = '$verificationType'.toLowerCase();
                            var selects = document.querySelectorAll('select');
                            var vSelect = null;
                            for (var s = 0; s < selects.length; s++) {
                                var opts = selects[s].options;
                                for (var o = 0; o < opts.length; o++) {
                                    var oText = opts[o].text.toLowerCase();
                                    if (oText.includes('father') || oText.includes('mother') || oText.includes('abc')) {
                                        vSelect = selects[s];
                                        break;
                                    }
                                }
                                if (vSelect) break;
                            }

                            if (vSelect) {
                                for (var k = 0; k < vSelect.options.length; k++) {
                                    var txt = vSelect.options[k].text.toLowerCase();
                                    var match = false;
                                    if (targetType.includes('father') && txt.includes('father')) match = true;
                                    else if (targetType.includes('mother') && txt.includes('mother')) match = true;
                                    else if (targetType.includes('abc') && (txt.includes('abc') || txt.includes('id'))) match = true;

                                    if (match) {
                                        vSelect.selectedIndex = k;
                                        vSelect.value = vSelect.options[k].value;
                                        vSelect.dispatchEvent(new Event('change', { bubbles: true }));
                                        vSelect.dispatchEvent(new Event('input', { bubbles: true }));
                                        console.log('MSRIT GO: Selected verification option:', vSelect.options[k].text);
                                        break;
                                    }
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

                            // 2. Fill the 4 digits
                            var digits = '$cleanDigits';
                            if (digits.length >= 4) {
                                var allInputs = Array.from(document.querySelectorAll('input[type="text"], input[type="tel"], input[type="number"], input[type="password"]'))
                                    .filter(function(i) {
                                        return i.id !== 'username' && i.id !== 'dd' && i.id !== 'mm' && i.id !== 'yyyy';
                                    });

                                console.log('MSRIT GO: Found', allInputs.length, 'potential digit input boxes');

                                if (allInputs.length >= 4) {
                                    // 4 individual digit boxes
                                    for (var d = 0; d < 4; d++) {
                                        allInputs[d].value = digits.charAt(d);
                                        allInputs[d].dispatchEvent(new Event('input', { bubbles: true }));
                                        allInputs[d].dispatchEvent(new Event('change', { bubbles: true }));
                                        allInputs[d].dispatchEvent(new KeyboardEvent('keyup', { key: digits.charAt(d), bubbles: true }));
                                    }
                                } else if (allInputs.length > 0) {
                                    // Single input field for 4 digits
                                    allInputs[0].value = digits;
                                    allInputs[0].dispatchEvent(new Event('input', { bubbles: true }));
                                    allInputs[0].dispatchEvent(new Event('change', { bubbles: true }));
                                }
                            }

                            // 3. Submit verification
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
                                    console.log('MSRIT GO: Clicking verification submit button');
                                    submitBtn.click();
                                }
                            }, 500);

                            return;
                        }

                        // CASE 2: Initial Login Page (USN + DOB)
                        var uInput = document.getElementById('username');
                        var dSelect = document.getElementById('dd');
                        if (uInput && dSelect) {
                            console.log('MSRIT GO: Detected Initial Login Page, autofilling USN & DOB...');
                            if (window.MsritBridge) {
                                window.MsritBridge.postMessage('progress', 'Authenticating USN & Date of Birth...');
                            }

                            uInput.value = '$usn';
                            uInput.dispatchEvent(new Event('input', { bubbles: true }));
                            
                            for (var i = 0; i < dSelect.options.length; i++) {
                                if (dSelect.options[i].value.trim() === '$cleanDay') {
                                    dSelect.selectedIndex = i;
                                    break;
                                }
                            }

                            var mSelect = document.getElementById('mm');
                            if (mSelect) {
                                for (var j = 0; j < mSelect.options.length; j++) {
                                    if (mSelect.options[j].value.trim() === '$cleanMonth') {
                                        mSelect.selectedIndex = j;
                                        break;
                                    }
                                }
                            }

                            var ySelect = document.getElementById('yyyy');
                            if (ySelect) {
                                for (var k = 0; k < ySelect.options.length; k++) {
                                    if (ySelect.options[k].value.trim() === '$cleanYear') {
                                        ySelect.selectedIndex = k;
                                        break;
                                    }
                                }
                                if (typeof putdate === 'function') {
                                    putdate();
                                }
                            }

                            // Submit initial login form
                            var loginBtn = document.querySelector('.cn-login-btn') || 
                                           document.querySelector('input[type="submit"]') ||
                                           document.querySelector('#login-form button[type="submit"]');

                            if (loginBtn) {
                                console.log('MSRIT GO: Submitting initial login form...');
                                loginBtn.click();
                            } else {
                                var form = document.getElementById('login-form') || document.querySelector('form.cn-landing-login');
                                if (form) form.submit();
                            }
                            return;
                        }

                        // CASE 3: Authenticated Dashboard / Records Screen!
                        console.log('MSRIT GO: On authenticated page, running DOM scraper...');
                        if (window.extractMsritData) {
                            window.extractMsritData();
                        }
                    } catch (e) {
                        console.error('MSRIT GO AutoSubmit Error:', e);
                        if (window.MsritBridge) {
                            window.MsritBridge.onError('Automation error: ' + e);
                        }
                    }
                })();
            """.trimIndent()
        }

        fun getScraperScript(): String {
            return """
                (function() {
                    try {
                        console.log("MSRIT GO: Running portal DOM extractor...");
                        var bodyText = document.body ? document.body.innerText : "";

                        // Check if still on login or verification form
                        var hasLoginForm = document.getElementById('username') !== null && 
                                           document.getElementById('dd') !== null;
                        var isVerificationScreen = bodyText.includes('Select Verification Type') || 
                                                   bodyText.includes('Enter Last 4 Digits');
                        
                        var errorAlert = document.querySelector('.uk-alert-danger') || document.querySelector('.alert-error');
                        if (errorAlert && errorAlert.innerText.trim().length > 0) {
                            var err = errorAlert.innerText.trim();
                            if (window.MsritBridge) {
                                window.MsritBridge.onError(err);
                            }
                            return;
                        }

                        if (hasLoginForm || isVerificationScreen) {
                            console.log("MSRIT GO: Still in authentication flow, deferring extraction");
                            return;
                        }

                        if (window.MsritBridge) {
                            window.MsritBridge.postMessage('progress', 'Extracting academic profile & records...');
                        }

                        // --- Extract Profile ---
                        var usnMatch = bodyText.match(/1MS\d{2}[A-Z]{2}\d{3}/i);
                        var detectedUsn = usnMatch ? usnMatch[0].toUpperCase() : "";

                        var detectedName = "";
                        var nameMatch = bodyText.match(/(?:Student Name|Name)\s*[:\-]\s*([A-Za-z\s.]+)/i);
                        if (nameMatch && nameMatch[1]) {
                            detectedName = nameMatch[1].trim();
                        } else {
                            var heading = document.querySelector('.uk-article-title, .cn-student-name, h3, h4');
                            if (heading) detectedName = heading.innerText.trim();
                        }
                        if (!detectedName || detectedName.length < 2 || detectedName.toLowerCase().includes('welcome')) {
                            detectedName = "MSRIT Student";
                        }

                        var detectedSem = 5;
                        var semMatch = bodyText.match(/(\d)(?:st|nd|rd|th)?\s*Sem(?:ester)?/i);
                        if (semMatch) {
                            detectedSem = parseInt(semMatch[1]);
                        }

                        var detectedBranch = "Computer Science & Engineering";
                        var branchMatch = bodyText.match(/(?:Department|Branch|Programme)\s*[:\-]\s*([A-Za-z\s&]+)/i);
                        if (branchMatch) {
                            detectedBranch = branchMatch[1].trim();
                        } else if (detectedUsn.includes("CS")) {
                            detectedBranch = "Computer Science & Engineering";
                        } else if (detectedUsn.includes("IS")) {
                            detectedBranch = "Information Science & Engineering";
                        } else if (detectedUsn.includes("EC")) {
                            detectedBranch = "Electronics & Communication Engg";
                        } else if (detectedUsn.includes("AI") || detectedUsn.includes("AD")) {
                            detectedBranch = "Artificial Intelligence & Data Science";
                        } else if (detectedUsn.includes("CI")) {
                            detectedBranch = "Computer Science (Cyber Security)";
                        }

                        var detectedSec = "A";
                        var secMatch = bodyText.match(/Sec(?:tion)?\s*[:\-]\s*([A-Z])/i);
                        if (secMatch) detectedSec = secMatch[1].toUpperCase();

                        var detectedProctor = "Dept Faculty Mentor";
                        var proctorMatch = bodyText.match(/Proctor\s*(?:Name)?\s*[:\-]\s*([A-Za-z\s.]+)/i);
                        if (proctorMatch) detectedProctor = proctorMatch[1].trim();

                        // --- Extract Attendance ---
                        var attendanceList = [];
                        var tables = document.querySelectorAll('table');

                        tables.forEach(function(table) {
                            var tText = table.innerText.toLowerCase();
                            if (tText.includes('attended') || tText.includes('held') || tText.includes('attendance') || tText.includes('%')) {
                                var rows = table.querySelectorAll('tr');
                                var hIndices = { code: -1, title: -1, attended: -1, total: -1, pct: -1 };

                                rows.forEach(function(row, rIdx) {
                                    var ths = row.querySelectorAll('th, td');
                                    if (rIdx === 0 || hIndices.code === -1) {
                                        ths.forEach(function(th, cIdx) {
                                            var txt = th.innerText.toLowerCase().trim();
                                            if (txt.includes('code') || txt.includes('course id')) hIndices.code = cIdx;
                                            else if (txt.includes('name') || txt.includes('title') || txt.includes('subject') || txt.includes('course')) hIndices.title = cIdx;
                                            else if (txt.includes('attended') || txt.includes('present')) hIndices.attended = cIdx;
                                            else if (txt.includes('held') || txt.includes('total') || txt.includes('conducted')) hIndices.total = cIdx;
                                            else if (txt.includes('percentage') || txt.includes('%')) hIndices.pct = cIdx;
                                        });
                                    }

                                    if (ths.length >= 3 && rIdx > 0) {
                                        var code = hIndices.code >= 0 && ths[hIndices.code] ? ths[hIndices.code].innerText.trim() : "";
                                        var title = hIndices.title >= 0 && ths[hIndices.title] ? ths[hIndices.title].innerText.trim() : "";
                                        var attendedStr = hIndices.attended >= 0 && ths[hIndices.attended] ? ths[hIndices.attended].innerText.trim() : "";
                                        var totalStr = hIndices.total >= 0 && ths[hIndices.total] ? ths[hIndices.total].innerText.trim() : "";

                                        // Fallback column identification
                                        if (!code || !title) {
                                            ths.forEach(function(c) {
                                                var val = c.innerText.trim();
                                                if (/^[A-Z0-9]{4,8}$/i.test(val) && !code) code = val;
                                                else if (val.length > 5 && isNaN(val) && !title && !val.includes('%')) title = val;
                                            });
                                        }

                                        var attended = parseInt(attendedStr) || 0;
                                        var total = parseInt(totalStr) || 0;

                                        if (code && (total > 0 || title.length > 3)) {
                                            attendanceList.push({
                                                code: code,
                                                title: title || code,
                                                attended: attended,
                                                total: total > 0 ? total : attended,
                                                credits: 4,
                                                faculty: "Dept Faculty",
                                                type: code.toLowerCase().includes('l') ? "Practical" : "Theory"
                                            });
                                        }
                                    }
                                });
                            }
                        });

                        // --- Extract CIE Marks ---
                        var cieMarksList = [];
                        tables.forEach(function(table) {
                            var tText = table.innerText.toLowerCase();
                            if (tText.includes('cie') || tText.includes('internal') || tText.includes('test 1') || tText.includes('marks')) {
                                var rows = table.querySelectorAll('tr');
                                rows.forEach(function(row) {
                                    var cells = row.querySelectorAll('td');
                                    if (cells.length >= 4) {
                                        var code = cells[0].innerText.trim();
                                        var title = cells[1].innerText.trim();
                                        var nums = [];
                                        for (var c = 2; c < cells.length; c++) {
                                            var val = parseFloat(cells[c].innerText.trim());
                                            if (!isNaN(val)) nums.push(val);
                                        }
                                        if (/^[A-Z0-9]{4,8}$/i.test(code) && nums.length > 0) {
                                            cieMarksList.push({
                                                code: code,
                                                title: title || code,
                                                credits: 4,
                                                cie1: nums.length > 0 ? nums[0] : 40.0,
                                                cie2: nums.length > 1 ? nums[1] : 42.0,
                                                cie3: nums.length > 2 ? nums[2] : null,
                                                assignment: 9.0,
                                                quiz: 9.0,
                                                totalInternal: nums[nums.length - 1]
                                            });
                                        }
                                    }
                                });
                            }
                        });

                        // --- Extract Circulars ---
                        var circulars = [];
                        var noticeNodes = document.querySelectorAll('.cn-events li, .cn-alert-circulars a, ul.uk-list li');
                        noticeNodes.forEach(function(node, idx) {
                            var text = node.innerText.trim();
                            var a = node.querySelector('a');
                            var url = a ? a.href : "https://parents.msrit.edu/newparents/index.php";
                            if (text.length > 10) {
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

                        console.log("MSRIT GO: Extracted data payload: ", payload);

                        if (window.MsritBridge) {
                            window.MsritBridge.onDataExtracted(JSON.stringify(payload));
                        }
                    } catch (err) {
                        console.error("MSRIT GO Extraction Exception:", err);
                        if (window.MsritBridge) {
                            window.MsritBridge.onError("Extraction error: " + err);
                        }
                    }
                })();
            """.trimIndent()
        }

        fun getStyleEnhancementScript(): String {
            return """
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = `
                        /* Modern responsive enhancements injected by MSRIT GO */
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important;
                            background-color: #0F1117 !important;
                            color: #F1F5F9 !important;
                        }
                        .cn-landing {
                            padding: 10px !important;
                        }
                        .uk-card-default {
                            background: #1A1D27 !important;
                            border: 1px solid #272B3B !important;
                            border-radius: 16px !important;
                            box-shadow: 0 4px 20px rgba(0,0,0,0.3) !important;
                        }
                        .cn-login-btn {
                            background: #B82226 !important;
                            border-radius: 12px !important;
                            font-weight: bold !important;
                            letter-spacing: 0.5px !important;
                        }
                        .uk-input, .uk-select {
                            background: #141824 !important;
                            border: 1px solid #2C334D !important;
                            color: #FFFFFF !important;
                            border-radius: 8px !important;
                        }
                        table.uk-table {
                            background: #141824 !important;
                            border-radius: 12px !important;
                            overflow: hidden !important;
                            border: 1px solid #2A324B !important;
                        }
                        table.uk-table th {
                            background: #1E2336 !important;
                            color: #38BDF8 !important;
                            font-weight: bold !important;
                        }
                        table.uk-table td {
                            color: #F8FAFC !important;
                            border-bottom: 1px solid #2A324B !important;
                        }
                    `;
                    document.head.appendChild(style);
                })();
            """.trimIndent()
        }
    }
}
