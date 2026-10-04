package edu.msrit.go.data

import android.webkit.JavascriptInterface

class PortalBridge(
    private val onPageTitleReceived: (String) -> Unit = {},
    private val onLoginDetected: (String) -> Unit = {}
) {

    @JavascriptInterface
    fun postMessage(type: String, message: String) {
        if (type == "login") {
            onLoginDetected(message)
        } else if (type == "title") {
            onPageTitleReceived(message)
        }
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
                    } catch (e) {
                        console.error('MSRIT GO Autofill Error:', e);
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
                    `;
                    document.head.appendChild(style);
                })();
            """.trimIndent()
        }
    }
}
