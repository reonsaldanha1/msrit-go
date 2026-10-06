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

        fun getAutoSubmitScript(usn: String, day: String, month: String, year: String): String {
            val cleanDay = day.trim().padStart(2, '0')
            val cleanMonth = month.trim().padStart(2, '0')
            val cleanYear = year.trim()

            return """
                (function() {
                    try {
                        // 1. Fill fields
                        var uInput = document.getElementById('username');
                        if (uInput) {
                            uInput.value = '$usn';
                            uInput.dispatchEvent(new Event('input', { bubbles: true }));
                        }
                        
                        var dSelect = document.getElementById('dd');
                        if (dSelect) {
                            for (var i = 0; i < dSelect.options.length; i++) {
                                if (dSelect.options[i].value.trim() === '$cleanDay') {
                                    dSelect.selectedIndex = i;
                                    break;
                                }
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

                        // Check for error alerts first
                        var errEl = document.querySelector('.uk-alert-danger');
                        if (errEl && errEl.innerText.trim().length > 0) {
                            if (window.MsritBridge) {
                                window.MsritBridge.onError(errEl.innerText.trim());
                            }
                            return;
                        }

                        // Check if reCAPTCHA challenge is active
                        var recaptchaFrame = document.querySelector('iframe[src*="recaptcha"]');
                        if (recaptchaFrame && recaptchaFrame.style.visibility !== 'hidden' && recaptchaFrame.offsetHeight > 50) {
                            if (window.MsritBridge) {
                                window.MsritBridge.onNeedVerification();
                            }
                            return;
                        }

                        // 2. Submit form
                        if (window.MsritBridge) {
                            window.MsritBridge.postMessage('progress', 'Authenticating with MSRIT portal...');
                        }

                        var submitBtn = document.querySelector('.cn-login-btn') || 
                                        document.querySelector('input[type="submit"]') ||
                                        document.querySelector('#login-form button[type="submit"]');

                        if (submitBtn) {
                            submitBtn.click();
                        } else {
                            var form = document.getElementById('login-form') || document.querySelector('form.cn-landing-login');
                            if (form) form.submit();
                        }
                    } catch (e) {
                        console.error('MSRIT GO AutoSubmit Error:', e);
                        if (window.MsritBridge) {
                            window.MsritBridge.onError('Login form submission failed: ' + e);
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

                        // Check if still stuck on login form
                        var hasLoginForm = document.getElementById('username') !== null && 
                                           document.getElementById('dd') !== null;
                        
                        var errorAlert = document.querySelector('.uk-alert-danger') || document.querySelector('.alert-error');
                        if (errorAlert && errorAlert.innerText.trim().length > 0) {
                            var err = errorAlert.innerText.trim();
                            if (window.MsritBridge) {
                                window.MsritBridge.onError(err);
                            }
                            return;
                        }

                        if (hasLoginForm) {
                            console.log("MSRIT GO: Still on login page, skipping data extraction");
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
