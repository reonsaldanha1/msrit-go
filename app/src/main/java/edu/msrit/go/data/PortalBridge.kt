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

                                    console.log('MSRIT GO: Authenticated page confirmed! Waiting for Attention popup...');
                                    showBanner('MSRIT GO: Portal Connected! Handling Attention notice...', true);

                                    if (window.MsritBridge) {
                                        window.MsritBridge.postMessage('progress', 'Connected to Portal! Checking for Attention popup...');
                                    }

                                    // Functions for closing Attention popup and scrolling page
                                    window.__msritCloseAttention = function() {
                                        try {
                                            console.log("MSRIT GO: Searching for Attention popup...");
                                            var closedAny = false;
                                            var modals = document.querySelectorAll('.uk-modal, .modal, div[role="dialog"], #modal-overflow, #myloginModal, .uk-open, [uk-modal], div.uk-flex-top, .uk-dialog, div[class*="modal"], div[id*="modal"], div[id*="popup"], div[class*="popup"]');
                                            modals.forEach(function(modal) {
                                                var text = (modal.innerText || '').toLowerCase();
                                                var isAttention = text.includes('attention') || text.includes('registration') || text.includes('circular') || text.includes('important') || text.includes('notice');
                                                var isVisible = modal.classList.contains('uk-open') || modal.style.display === 'block' || (window.getComputedStyle(modal).display !== 'none' && window.getComputedStyle(modal).visibility !== 'hidden');

                                                if (isAttention || isVisible) {
                                                    var closeSelectors = [
                                                        '.uk-modal-close-default',
                                                        '.uk-modal-close',
                                                        '[uk-close]',
                                                        '.uk-close',
                                                        '.close',
                                                        '[data-uk-modal-close]',
                                                        'button[type="button"]',
                                                        'button',
                                                        'a.uk-close',
                                                        'a'
                                                    ];

                                                    for (var s = 0; s < closeSelectors.length; s++) {
                                                        var btns = modal.querySelectorAll(closeSelectors[s]);
                                                        for (var b = 0; b < btns.length; b++) {
                                                            var bText = btns[b].innerText.trim().toLowerCase();
                                                            var hasCloseAttr = btns[b].hasAttribute('uk-close') || btns[b].classList.contains('uk-close') || btns[b].classList.contains('uk-modal-close') || btns[b].classList.contains('uk-modal-close-default');
                                                            if (hasCloseAttr || bText === 'close' || bText === 'dismiss' || bText === 'ok' || bText === 'cancel' || bText === '×' || bText === 'x' || bText === 'proceed' || bText.includes('close')) {
                                                                console.log("MSRIT GO: Clicking close on Attention modal:", btns[b]);
                                                                btns[b].click();
                                                                closedAny = true;
                                                                break;
                                                            }
                                                        }
                                                        if (closedAny) break;
                                                    }

                                                    modal.classList.remove('uk-open');
                                                    modal.style.display = 'none';
                                                }
                                            });

                                            // Also search document-wide for any button with text "Close" or uk-close if modal exists
                                            var docButtons = document.querySelectorAll('button, a, input[type="button"]');
                                            for (var i = 0; i < docButtons.length; i++) {
                                                var dBtn = docButtons[i];
                                                var dText = (dBtn.innerText || dBtn.textContent || dBtn.value || '').trim().toLowerCase();
                                                var isCloseBtn = dText === 'close' || dText === 'dismiss' || dBtn.hasAttribute('uk-close') || dBtn.classList.contains('uk-close') || dBtn.classList.contains('uk-modal-close');
                                                if (isCloseBtn) {
                                                    var parentM = dBtn.closest('.uk-modal, .modal, div[role="dialog"], #modal-overflow, .uk-open');
                                                    if (parentM) {
                                                        try { dBtn.click(); closedAny = true; } catch(e){}
                                                    }
                                                }
                                            }

                                            if (window.UIkit && window.UIkit.modal) {
                                                try {
                                                    if (window.UIkit.modal('#modal-overflow')) {
                                                        window.UIkit.modal('#modal-overflow').hide();
                                                    }
                                                    var openModals = document.querySelectorAll('.uk-modal.uk-open');
                                                    openModals.forEach(function(om) {
                                                        window.UIkit.modal(om).hide();
                                                    });
                                                    closedAny = true;
                                                } catch (ue) {
                                                    console.log("UIkit API notice:", ue);
                                                }
                                            }

                                            // Unlock page scroll & clean backdrops
                                            document.documentElement.classList.remove('uk-modal-page');
                                            document.body.classList.remove('uk-modal-page');
                                            document.documentElement.style.overflow = 'auto';
                                            document.body.style.overflow = 'auto';

                                            var backdrops = document.querySelectorAll('.uk-modal-page, .modal-backdrop, #blackOverlay, .uk-modal-backdrop');
                                            backdrops.forEach(function(bd) {
                                                bd.classList.remove('uk-modal-page');
                                                if (bd.id === 'blackOverlay' || bd.classList.contains('uk-modal-backdrop') || bd.classList.contains('modal-backdrop')) {
                                                    bd.style.display = 'none';
                                                }
                                            });

                                            return closedAny;
                                        } catch (e) {
                                            console.error("MSRIT GO closeAttention error:", e);
                                            return false;
                                        }
                                    };

                                    window.__msritScrollPage = function(callback) {
                                        try {
                                            console.log("MSRIT GO: Scrolling entire page...");
                                            if (window.MsritBridge) {
                                                window.MsritBridge.postMessage('progress', 'Scrolling page to render all academic records...');
                                            }

                                            var totalHeight = Math.max(
                                                document.body ? document.body.scrollHeight : 0,
                                                document.documentElement ? document.documentElement.scrollHeight : 0,
                                                document.body ? document.body.offsetHeight : 0,
                                                document.documentElement ? document.documentElement.offsetHeight : 0,
                                                1200
                                            );

                                            var currentY = 0;
                                            var step = Math.max(150, Math.floor(totalHeight / 15));
                                            var scrollTimer = setInterval(function() {
                                                currentY += step;
                                                window.scrollTo(0, currentY);

                                                if (currentY >= totalHeight) {
                                                    clearInterval(scrollTimer);
                                                    setTimeout(function() {
                                                        window.scrollTo(0, 0);
                                                        setTimeout(function() {
                                                            if (window.__msritCloseAttention) window.__msritCloseAttention();
                                                            window.__msritAlreadyScrolled = true;
                                                            setTimeout(function() { window.__msritAlreadyScrolled = false; }, 15000);
                                                            if (callback) callback();
                                                        }, 400);
                                                    }, 400);
                                                }
                                            }, 60);
                                        } catch (err) {
                                            console.error("MSRIT GO scrollPage error:", err);
                                            if (callback) callback();
                                        }
                                    };

                                    // Active polling for Attention popup (up to 3 seconds), then scroll entire page, then extract
                                    var attentionPollCount = 0;
                                    var maxAttentionPolls = 15; // 15 * 200ms = 3.0 seconds max wait
                                    var attentionHandled = false;

                                    function proceedWithScrollAndExtract() {
                                        showBanner('MSRIT GO: Scrolling entire page to load records...', true);
                                        if (window.MsritBridge) {
                                            window.MsritBridge.postMessage('progress', 'Scrolling page to render attendance & CIE marks...');
                                        }

                                        window.__msritScrollPage(function() {
                                            showBanner('MSRIT GO: Page scrolled! Extracting records...', true);
                                            if (window.MsritBridge) {
                                                window.MsritBridge.postMessage('progress', 'Page rendered! Extracting records...');
                                            }

                                            setTimeout(function() {
                                                showBanner('MSRIT GO: Syncing attendance & CIE marks...', true);
                                                ${getScraperScript()}
                                            }, 500);
                                        });
                                    }

                                    var attentionInterval = setInterval(function() {
                                        attentionPollCount++;
                                        var closed = window.__msritCloseAttention();
                                        if (closed && !attentionHandled) {
                                            attentionHandled = true;
                                            clearInterval(attentionInterval);
                                            console.log("MSRIT GO: Closed Attention notice at check " + attentionPollCount);
                                            showBanner('MSRIT GO: Closed Attention notice. Preparing to scroll...', true);
                                            if (window.MsritBridge) {
                                                window.MsritBridge.postMessage('progress', 'Closed Attention notice. Preparing to scroll page...');
                                            }
                                            setTimeout(function() {
                                                proceedWithScrollAndExtract();
                                            }, 500);
                                        } else if (attentionPollCount >= maxAttentionPolls) {
                                            clearInterval(attentionInterval);
                                            if (!attentionHandled) {
                                                attentionHandled = true;
                                                window.__msritCloseAttention();
                                                proceedWithScrollAndExtract();
                                            }
                                        }
                                    }, 200);
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

                            var detectedSem = 1;
                            var semMatch = bodyText.match(/(?:SEM|Sem|Semester)\s*0?(\d+)/i) || bodyText.match(/(\d)(?:st|nd|rd|th)?\s*Sem(?:ester)?/i);
                            if (semMatch) {
                                detectedSem = parseInt(semMatch[1]);
                            }

                            var detectedBranch = "Computer Science & Engineering (AIML)";
                            var branchMatch = bodyText.match(/(?:Department|Branch|Programme|Course)\s*[:\-]\s*([A-Za-z\s&()]+)/i);
                            if (branchMatch && branchMatch[1].trim().length > 4) {
                                detectedBranch = branchMatch[1].trim().split('\n')[0].trim();
                            } else if (detectedUsn.includes("CI")) {
                                detectedBranch = "Computer Science & Engineering (AIML)";
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

                            var detectedSec = "G";
                            var secMatch = bodyText.match(/Sec(?:tion)?\s*[:\-]\s*([A-Z])/i);
                            if (secMatch) detectedSec = secMatch[1].toUpperCase();

                            var detectedProctor = "Sini Anna Alex";
                            var detectedProctorEmail = "sinialex@msrit.edu";
                            var detectedProctorPhone = "9901287316";
                            var detectedProctorCabin = "First Year Faculty";

                            var proctorCard = document.querySelector('.cn-proctor-notes .md-card-head-text, .cn-lec-info h3');
                            if (proctorCard) {
                                var pLines = (proctorCard.innerText || '').split('\n').map(function(s){ return s.trim(); }).filter(Boolean);
                                if (pLines.length > 0 && pLines[0]) detectedProctor = pLines[0].replace(/\s{2,}/g, ' ');
                                if (pLines.length > 2 && pLines[2].includes('@')) detectedProctorEmail = pLines[2];
                                if (pLines.length > 3 && /\d{10}/.test(pLines[3])) detectedProctorPhone = pLines[3];
                            } else {
                                var proctorMatch = bodyText.match(/(?:Proctor|Counselor|Mentor)\s*(?:Name)?\s*[:\-]\s*([A-Za-z\s.]+)/i);
                                if (proctorMatch) detectedProctor = proctorMatch[1].trim().split('\n')[0].trim();
                            }

                            // --- 2. Advanced Multi-strategy Table Parsing ---
                            var msritCourseCatalog = {
                                "22CI51": "Cryptography and Network Security", "21CI51": "Cryptography and Network Security", "CI510": "Cryptography and Network Security", "CI51": "Cryptography and Network Security",
                                "22CI52": "Computer Networks", "21CI52": "Computer Networks", "CI520": "Computer Networks", "CI52": "Computer Networks",
                                "22CI53": "Operating Systems and Virtualization", "21CI53": "Operating Systems and Virtualization", "CI530": "Operating Systems", "CI53": "Operating Systems",
                                "22CI54": "Database Management Systems", "21CI54": "Database Management Systems", "CI540": "Database Management Systems", "CI54": "Database Management Systems",
                                "22CIL56": "Network Security Laboratory", "21CIL56": "Network Security Laboratory", "CIL56": "Network Security Laboratory",
                                "22CIL57": "Database & OS Laboratory", "21CIL57": "Database & OS Laboratory",
                                "22CI61": "Cyber Forensics & Incident Response", "21CI61": "Cyber Forensics & Incident Response",
                                "22CI62": "Cloud Security and Privacy", "21CI62": "Cloud Security and Privacy",
                                "22CI63": "Web Application Security", "21CI63": "Web Application Security",
                                "22CI31": "Data Structures & Applications", "21CI31": "Data Structures & Applications",
                                "22CI32": "Analog & Digital Electronics", "22CI33": "Computer Organization & Architecture",
                                "22CI41": "Design & Analysis of Algorithms", "22CI42": "Microcontroller & Embedded Systems", "22CI43": "Information Security Fundamentals",
                                "22CS51": "Analysis and Design of Algorithms", "21CS51": "Analysis and Design of Algorithms", "CS510": "Analysis and Design of Algorithms", "CS51": "Analysis and Design of Algorithms",
                                "22CS52": "Database Management Systems", "21CS52": "Database Management Systems", "CS520": "Database Management Systems", "CS52": "Database Management Systems",
                                "22CS53": "Computer Networks", "21CS53": "Computer Networks", "CS530": "Computer Networks", "CS53": "Computer Networks",
                                "22CS54": "Artificial Intelligence & Machine Learning", "21CS54": "Artificial Intelligence & Machine Learning", "CS540": "Artificial Intelligence & Machine Learning", "CS54": "Artificial Intelligence & Machine Learning",
                                "22CS55": "Cloud Computing and Virtualization", "21CS55": "Cloud Computing and Virtualization", "CS550": "Cloud Computing and Virtualization", "CS55": "Cloud Computing and Virtualization",
                                "22CSL56": "DBMS & Networks Laboratory", "21CSL56": "DBMS & Networks Laboratory", "CSL56": "DBMS & Networks Laboratory",
                                "22CS57": "Constitution of India & Professional Ethics", "21CS57": "Constitution of India & Professional Ethics", "CS570": "Constitution of India & Professional Ethics",
                                "22CS61": "Compiler Design", "21CS61": "Compiler Design", "22CS62": "Software Engineering & Agile Methodology", "22CS63": "Web Technologies",
                                "22CS31": "Data Structures", "22CS32": "Digital Design & Computer Organization", "22CS41": "Operating Systems", "22CS42": "Object Oriented Programming with Java",
                                "22IS51": "Operating Systems & Architecture", "21IS51": "Operating Systems & Architecture", "22IS52": "Database Management Systems", "21IS52": "Database Management Systems",
                                "22IS53": "Computer Networks & Security", "21IS53": "Computer Networks & Security", "22IS54": "Theory of Computation", "21IS54": "Theory of Computation", "22ISL56": "OS & Database Laboratory",
                                "22AI51": "Machine Learning & Pattern Recognition", "21AI51": "Machine Learning & Pattern Recognition", "22AI52": "Deep Learning Architectures", "21AI52": "Deep Learning Architectures", "22AI53": "Natural Language Processing", "22AIL56": "Machine Learning Laboratory",
                                "22EC51": "Digital Signal Processing", "21EC51": "Digital Signal Processing", "22EC52": "Microcontroller & Embedded Systems", "22EC53": "Electromagnetic Waves & Transmission", "22ECL56": "DSP & Embedded Laboratory",
                                "22MAT11": "Calculus & Linear Algebra", "21MAT11": "Calculus & Linear Algebra", "22MAT21": "Advanced Calculus & Numerical Methods", "21MAT21": "Advanced Calculus & Numerical Methods", "22MAT31": "Transform Calculus & Fourier Series", "21MAT31": "Transform Calculus & Fourier Series", "22MAT41": "Complex Analysis & Probability", "21MAT41": "Complex Analysis & Probability",
                                "22HSS51": "Universal Human Values & Professional Ethics", "21HSS51": "Universal Human Values & Professional Ethics", "22CIP57": "Constitution of India & Cyber Law", "21CIP57": "Constitution of India & Cyber Law"
                            };

                            function cleanAndFormatTitle(cCode, rTitle) {
                                var t = (rTitle || '').replace(/\s+/g, ' ').trim();
                                if (cCode) {
                                    var escCode = cCode.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
                                    t = t.replace(new RegExp('^\\s*[\\[\\(]?\\s*' + escCode + '\\s*[\\]\\)]?\\s*[-–—:]?\\s*', 'i'), '').trim();
                                }
                                t = t.replace(/\[[A-Za-z0-9_-]+\]/gi, '').trim();

                                var isInvalid = !t || t.length < 3 || t.toUpperCase() === (cCode || '').toUpperCase() ||
                                                /^(theory|practical|integrated|lab|core|elective|view|details?|regular|credit|course\s*\d+)$/i.test(t) ||
                                                /^\d+$/.test(t);

                                if (isInvalid && cCode) {
                                    var upperC = cCode.toUpperCase();
                                    if (msritCourseCatalog[upperC]) return msritCourseCatalog[upperC];
                                    var shortC = upperC.replace(/^2[0-9]/, '');
                                    if (msritCourseCatalog[shortC]) return msritCourseCatalog[shortC];
                                    return "Course " + upperC;
                                }

                                if (t === t.toUpperCase() && t.length > 4 && /[A-Z]/.test(t)) {
                                    t = t.toLowerCase().split(' ').map(function(w) {
                                        if (w.length <= 2 && /^(of|in|to|and|&|on|for|at|by|with|a|an)$/i.test(w)) return w.toLowerCase();
                                        if (/^(dbms|os|ai|ml|cie|see|ug|pg|it|ip|iot|vtu|dsp)$/i.test(w)) return w.toUpperCase();
                                        return w.charAt(0).toUpperCase() + w.slice(1);
                                    }).join(' ');
                                }
                                return t;
                            }

                            var subjectMap = {}; // Key: code.toUpperCase() -> { attendance: {}, marks: {}, sessions: [] }

                            docList.forEach(function(doc) {
                                var tables = doc.querySelectorAll('table');
                                tables.forEach(function(table) {
                                    var rows = Array.from(table.querySelectorAll('tr'));
                                    if (rows.length < 2) return;

                                    var tableText = (table.innerText || '').toLowerCase();
                                    var isAttdTable = tableText.includes('attendance') || tableText.includes('classes held') || tableText.includes('classes attended') || tableText.includes('total classes') || tableText.includes('conducted');
                                    var isCieTable = tableText.includes('cie') || tableText.includes('internal') || tableText.includes('marks') || tableText.includes('ia-1') || tableText.includes('cie-1');
                                    var isRegTable = tableText.includes('course code') && (tableText.includes('course title') || tableText.includes('course name')) && tableText.includes('credit');

                                    if (!isAttdTable && !isCieTable && !isRegTable) return;

                                    // Find authentic header row
                                    var headerRowIdx = -1;
                                    for (var hr = 0; hr < Math.min(4, rows.length); hr++) {
                                        var rText = (rows[hr].innerText || '').toLowerCase();
                                        var hasTh = rows[hr].querySelectorAll('th').length > 0;
                                        var hasKeywords = (rText.includes('code') || rText.includes('course') || rText.includes('subject')) &&
                                                          (rText.includes('title') || rText.includes('name') || rText.includes('held') || rText.includes('attended') || rText.includes('marks') || rText.includes('credit') || rText.includes('%'));
                                        if (hasTh || hasKeywords) {
                                            headerRowIdx = hr;
                                            break;
                                        }
                                    }
                                    if (headerRowIdx === -1) headerRowIdx = 0;

                                    var headerCells = Array.from(rows[headerRowIdx].querySelectorAll('th, td'));
                                    var headerTexts = [];
                                    var colPtr = 0;
                                    headerCells.forEach(function(c) {
                                        var span = parseInt(c.getAttribute('colspan')) || 1;
                                        var txt = (c.innerText || '').trim().toLowerCase();
                                        for (var s = 0; s < span; s++) {
                                            headerTexts[colPtr++] = txt;
                                        }
                                    });

                                    // Identify column roles
                                    var colRoles = {
                                        sno: -1,
                                        code: -1,
                                        title: -1,
                                        courseComb: -1,
                                        credits: -1,
                                        faculty: -1,
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
                                        if (colRoles.code === -1 && (ht.includes('course code') || ht.includes('sub code') || ht.includes('sub. code') || ht.includes('subject code') || ht.includes('course id') || ht === 'code')) {
                                            colRoles.code = idx;
                                        } else if (colRoles.title === -1 && (ht.includes('course title') || ht.includes('course name') || ht.includes('subject title') || ht.includes('subject name') || ht.includes('paper name') || ht.includes('description') || ht === 'title')) {
                                            colRoles.title = idx;
                                        } else if (colRoles.courseComb === -1 && (ht === 'course' || ht === 'subject' || ht.includes('course / subject') || ht.includes('subject / course'))) {
                                            colRoles.courseComb = idx;
                                        } else if (colRoles.credits === -1 && ht.includes('credit')) {
                                            colRoles.credits = idx;
                                        } else if (colRoles.faculty === -1 && (ht.includes('faculty') || ht.includes('staff') || ht.includes('teacher') || ht.includes('instructor'))) {
                                            colRoles.faculty = idx;
                                        } else if (colRoles.held === -1 && (ht.includes('classes held') || ht.includes('conducted') || ht.includes('total classes') || ht.includes('total hours') || ht === 'held' || ht === 'total')) {
                                            colRoles.held = idx;
                                        } else if (colRoles.attended === -1 && (ht.includes('classes attended') || ht.includes('hours attended') || ht.includes('attended') || ht.includes('present'))) {
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

                                    if (colRoles.code === -1 && colRoles.courseComb >= 0 && colRoles.title >= 0) {
                                        colRoles.code = colRoles.courseComb;
                                    } else if (colRoles.title === -1 && colRoles.courseComb >= 0 && colRoles.code >= 0) {
                                        colRoles.title = colRoles.courseComb;
                                    }

                                    // Iterate data rows
                                    for (var rIdx = headerRowIdx + 1; rIdx < rows.length; rIdx++) {
                                        var row = rows[rIdx];
                                        var cells = Array.from(row.querySelectorAll('td'));
                                        if (cells.length < 2) continue;

                                        var rowText = (row.innerText || '').trim();
                                        if ((rowText.toLowerCase().includes('total') || rowText.toLowerCase().includes('average')) && cells.length < 5) continue;

                                        // 1. Check buttons/links for query params or onclick args
                                        var btnCode = "";
                                        var btnTitle = "";
                                        var rowBtns = Array.from(row.querySelectorAll('a, button, input[type="button"], .uk-button'));
                                        for (var b = 0; b < rowBtns.length; b++) {
                                            var bElem = rowBtns[b];
                                            var bStr = (bElem.getAttribute('onclick') || '') + ' ' + (bElem.getAttribute('href') || '');
                                            var cMatch = bStr.match(/[?&;](?:sub_code|course_code|subcode|subid|sub_id|code)=([A-Za-z0-9_-]+)/i);
                                            if (cMatch && cMatch[1]) {
                                                var cCand = decodeURIComponent(cMatch[1]).trim().toUpperCase();
                                                if (!cCand.startsWith('1MS') && cCand.length >= 3 && cCand.length <= 12) {
                                                    btnCode = cCand;
                                                }
                                            }
                                            var tMatch = bStr.match(/[?&;](?:sub_name|course_name|subname|title|subject)=([^&'"]+)/i);
                                            if (tMatch && tMatch[1]) {
                                                var tCand = decodeURIComponent(tMatch[1]).replace(/\+/g, ' ').trim();
                                                if (tCand.length > 3 && isNaN(tCand)) {
                                                    btnTitle = tCand;
                                                }
                                            }
                                            var fnMatch = bStr.match(/(?:popUp|view|show|detail)\s*\(\s*['"]([A-Za-z0-9_-]{3,12})['"]\s*,\s*['"]([^'"]+)['"]/i);
                                            if (fnMatch) {
                                                if (!btnCode && !fnMatch[1].toUpperCase().startsWith('1MS')) btnCode = fnMatch[1].toUpperCase();
                                                if (!btnTitle && fnMatch[2].length > 3) btnTitle = fnMatch[2].trim();
                                            }
                                        }

                                        // 2. Extract from designated column cells
                                        var cellCode = "";
                                        var cellTitle = "";
                                        if (colRoles.code >= 0 && cells[colRoles.code]) {
                                            cellCode = cells[colRoles.code].innerText.trim();
                                        }
                                        if (colRoles.title >= 0 && cells[colRoles.title]) {
                                            cellTitle = cells[colRoles.title].innerText.trim();
                                        }
                                        if (colRoles.courseComb >= 0 && cells[colRoles.courseComb]) {
                                            var combVal = cells[colRoles.courseComb].innerText.trim();
                                            var combParts = combVal.split(/[\n\r]+|\s+[-–—:]\s+|\s*\[|\]\s*/);
                                            if (combParts.length >= 2) {
                                                var p0 = combParts[0].trim();
                                                if (!cellCode && /^[A-Z0-9-]{3,12}$/i.test(p0) && !p0.toUpperCase().startsWith('1MS')) {
                                                    cellCode = p0;
                                                    cellTitle = combParts.slice(1).join(' ').replace(/[\[\]]/g, '').trim();
                                                }
                                            } else if (!cellTitle) {
                                                cellTitle = combVal;
                                            }
                                        }

                                        // 3. Fallback scan across all cells for MSRIT course code
                                        if (!cellCode || cellCode.toUpperCase().startsWith('1MS') || !/^[A-Z0-9-]{3,12}$/i.test(cellCode) || /^(view|action|details?|sl|sno|\d+)$/i.test(cellCode)) {
                                            cellCode = "";
                                            for (var ci = 0; ci < cells.length; ci++) {
                                                var cVal = cells[ci].innerText.trim();
                                                var cm = cVal.match(/\b(2[0-9][A-Z]{2,4}[0-9]{2,3}[A-Z]?|[A-Z]{2,4}[0-9]{2,3}[A-Z]?|[A-Z]{2,4}L[0-9]{2,3}|2[0-9][A-Z]{2,4}L[0-9]{2,3})\b/i);
                                                if (cm && !cm[0].toUpperCase().startsWith('1MS')) {
                                                    cellCode = cm[0];
                                                    break;
                                                }
                                            }
                                        }

                                        // 4. Fallback scan across all cells for Course Title
                                        if (!cellTitle || cellTitle.length < 3 || cellTitle === cellCode) {
                                            cellTitle = "";
                                            for (var ti = 0; ti < cells.length; ti++) {
                                                var tVal = cells[ti].innerText.trim();
                                                var isAction = /^(view|action|details?|click|check|show)$/i.test(tVal);
                                                var isFaculty = /^(dr\.|prof\.|mr\.|mrs\.|ms\.)/i.test(tVal);
                                                var isNumber = /^\d+$/.test(tVal) || /^\d+\s*[\/-]\s*\d+$/.test(tVal);
                                                var isPct = tVal.includes('%');
                                                var isCode = tVal === cellCode || /^[A-Z0-9-]{3,10}$/i.test(tVal);
                                                var isDate = /\d{4}-\d{2}-\d{2}|\d{2}[\/-]\d{2}[\/-]\d{2,4}/.test(tVal);

                                                if (!isAction && !isFaculty && !isNumber && !isPct && !isCode && !isDate && tVal.length > 4) {
                                                    cellTitle = tVal;
                                                    break;
                                                }
                                            }
                                        }

                                        var finalCode = (btnCode || cellCode || "").trim().toUpperCase();
                                        var rawTitle = (btnTitle || cellTitle || "").trim();

                                        if (!finalCode || finalCode.length < 3 || finalCode.startsWith("1MS")) continue;

                                        var finalTitle = cleanAndFormatTitle(finalCode, rawTitle);

                                        // Faculty
                                        var faculty = "Dept Faculty";
                                        if (colRoles.faculty >= 0 && cells[colRoles.faculty]) {
                                            var fVal = cells[colRoles.faculty].innerText.trim();
                                            if (fVal.length > 3) faculty = fVal;
                                        } else {
                                            for (var fi = 0; fi < cells.length; fi++) {
                                                var cf = cells[fi].innerText.trim();
                                                if (/^(dr\.|prof\.|mr\.|mrs\.|ms\.)/i.test(cf) && cf.length > 4) {
                                                    faculty = cf;
                                                    break;
                                                }
                                            }
                                        }

                                        // Credits
                                        var credits = 4;
                                        if (colRoles.credits >= 0 && cells[colRoles.credits]) {
                                            var cr = parseInt(cells[colRoles.credits].innerText.trim());
                                            if (!isNaN(cr) && cr > 0 && cr <= 10) credits = cr;
                                        }

                                        if (!subjectMap[finalCode]) {
                                            subjectMap[finalCode] = {
                                                code: finalCode,
                                                title: finalTitle,
                                                credits: credits,
                                                faculty: faculty,
                                                type: (finalCode.toLowerCase().includes('l') || finalTitle.toLowerCase().includes('lab')) ? "Practical" : "Theory",
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

                                        var sub = subjectMap[finalCode];
                                        if (finalTitle.length > sub.title.length && !sub.title.includes(finalTitle)) {
                                            sub.title = finalTitle;
                                        }
                                        if (faculty !== "Dept Faculty" && sub.faculty === "Dept Faculty") {
                                            sub.faculty = faculty;
                                        }

                                        // --- Parse Attendance Numbers ---
                                        var parsedAttended = null;
                                        var parsedHeld = null;

                                        for (var ai = 0; ai < cells.length; ai++) {
                                            var cText = cells[ai].innerText.trim();
                                            var slashMatch = cText.match(/(\d+)\s*[\/]\s*(\d+)/);
                                            if (slashMatch) {
                                                parsedAttended = parseInt(slashMatch[1]);
                                                parsedHeld = parseInt(slashMatch[2]);
                                                break;
                                            }
                                        }

                                        if (parsedAttended === null || parsedHeld === null) {
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
                                                if (!isNaN(v) && v >= 0 && v <= 150 && idx !== colRoles.code && idx !== colRoles.credits) {
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
                                    cycle: "First Semester (UG)",
                                    academicYear: "2026 - 2027",
                                    proctorName: detectedProctor,
                                    proctorEmail: detectedProctorEmail,
                                    proctorCabin: detectedProctorCabin,
                                    proctorPhone: detectedProctorPhone
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

                    // Fallback definitions for closing attention modal & scrolling if not already on window
                    if (typeof window.__msritCloseAttention !== 'function') {
                        window.__msritCloseAttention = function() {
                            try {
                                var modals = document.querySelectorAll('.uk-modal, .modal, div[role="dialog"], #modal-overflow, #myloginModal, .uk-open, [uk-modal]');
                                modals.forEach(function(modal) {
                                    var closeBtns = modal.querySelectorAll('.uk-modal-close-default, .uk-modal-close, [uk-close], .uk-close, .close, button, a');
                                    for (var i = 0; i < closeBtns.length; i++) {
                                        var txt = (closeBtns[i].innerText || '').trim().toLowerCase();
                                        if (closeBtns[i].hasAttribute('uk-close') || closeBtns[i].classList.contains('uk-close') || txt === 'close' || txt === 'ok' || txt === 'dismiss' || txt === '×' || txt === 'x') {
                                            closeBtns[i].click();
                                            break;
                                        }
                                    }
                                    modal.classList.remove('uk-open');
                                    modal.style.display = 'none';
                                });
                                if (window.UIkit && window.UIkit.modal) {
                                    try {
                                        if (window.UIkit.modal('#modal-overflow')) window.UIkit.modal('#modal-overflow').hide();
                                    } catch(e){}
                                }
                                document.documentElement.classList.remove('uk-modal-page');
                                document.body.classList.remove('uk-modal-page');
                                document.documentElement.style.overflow = 'auto';
                                document.body.style.overflow = 'auto';
                            } catch(e){}
                        };
                    }

                    if (typeof window.__msritScrollPage !== 'function') {
                        window.__msritScrollPage = function(cb) {
                            try {
                                var h = Math.max(document.body ? document.body.scrollHeight : 0, document.documentElement ? document.documentElement.scrollHeight : 0, 1200);
                                var cy = 0;
                                var st = Math.max(150, Math.floor(h / 15));
                                var tm = setInterval(function() {
                                    cy += st;
                                    window.scrollTo(0, cy);
                                    if (cy >= h) {
                                        clearInterval(tm);
                                        setTimeout(function() {
                                            window.scrollTo(0, 0);
                                            setTimeout(function() { if (cb) cb(); }, 400);
                                        }, 400);
                                    }
                                }, 60);
                            } catch(e) { if (cb) cb(); }
                        };
                    }

                    // Start extraction: Close attention popup, scroll page if not recently scrolled, then run extraction
                    if (window.__msritAlreadyScrolled) {
                        runExtraction(0);
                    } else {
                        window.__msritCloseAttention();
                        window.__msritScrollPage(function() {
                            window.__msritAlreadyScrolled = true;
                            setTimeout(function() { window.__msritAlreadyScrolled = false; }, 15000);
                            runExtraction(0);
                        });
                    }
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
