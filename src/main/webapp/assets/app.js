(function () {
    function switchLoginPanel(name) {
        document.querySelectorAll("[data-login-tab]").forEach(function (item) {
            item.classList.toggle("active", item.getAttribute("data-login-tab") === name);
        });
        document.querySelectorAll("[data-login-panel]").forEach(function (panel) {
            panel.classList.toggle("hidden", panel.getAttribute("data-login-panel") !== name);
        });

        var heading = document.querySelector("[data-login-heading]");
        if (heading) {
            heading.textContent = name === "sms" ? "手机号验证码登录" : "用户名密码登录";
        }
    }

    function pageContext() {
        if (typeof window.BBS_CONTEXT === "string") {
            return window.BBS_CONTEXT;
        }
        var path = window.location.pathname;
        var context = "";
        ["/register", "/login", "/password/reset", "/post/detail"].some(function (suffix) {
            if (path.slice(-suffix.length) === suffix) {
                context = path.slice(0, path.length - suffix.length);
                return true;
            }
            return false;
        });
        return context;
    }

    var forcedLogoutShown = false;

    function handleForcedLogout(data) {
        if (!data || !data.banned) {
            return false;
        }
        if (!forcedLogoutShown) {
            forcedLogoutShown = true;
            alert(data.message || "账号已被封禁，请联系管理员");
        }
        window.location.replace(data.redirect || pageContext() + "/login");
        return true;
    }

    function checkSessionStatus() {
        if (!window.BBS_LOGGED_IN || forcedLogoutShown) {
            return;
        }
        fetch(pageContext() + "/session/status", {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "X-Requested-With": "fetch"
            },
            credentials: "same-origin"
        }).then(function (response) {
            return response.json();
        }).then(function (data) {
            handleForcedLogout(data);
        }).catch(function () {
        });
    }

    function showActionMessage(message, ok) {
        var result = document.getElementById("actionMessage");
        if (!result) {
            return;
        }
        result.textContent = message || "";
        result.classList.toggle("error-message", ok === false);
        result.classList.toggle("visible", Boolean(message));
        window.clearTimeout(result._hideTimer);
        result._hideTimer = window.setTimeout(function () {
            result.classList.remove("visible");
        }, 2200);
    }

    function setStatText(root, selector, value) {
        if (!root || value === undefined || value === null) {
            return;
        }
        var target = root.querySelector(selector);
        if (target) {
            target.textContent = String(value);
        }
    }

    function updatePostStats(post) {
        if (!post || !post.id) {
            return;
        }
        var root = document.querySelector("[data-post-stats][data-post-id='" + post.id + "']");
        setStatText(root, "[data-post-stat='likeScore']", post.likeScore);
        setStatText(root, "[data-post-stat='dislikeScore']", post.dislikeScore);
        setStatText(root, "[data-post-stat='favoriteCount']", post.favoriteCount);
        setStatText(root, "[data-post-stat='commentCount']", post.commentCount);
    }

    function updateCommentStats(comment) {
        if (!comment || !comment.id) {
            return;
        }
        var root = document.querySelector("[data-comment-stats][data-comment-id='" + comment.id + "']");
        setStatText(root, "[data-comment-stat='likeScore']", comment.likeScore);
        setStatText(root, "[data-comment-stat='dislikeScore']", comment.dislikeScore);
    }

    document.addEventListener("click", function (event) {
        var disclosureButton = event.target.closest("[data-disclosure-toggle]");
        if (disclosureButton) {
            event.preventDefault();
            var disclosureForm = disclosureButton.closest("[data-disclosure-form]");
            if (disclosureForm) {
                var fields = disclosureForm.querySelector("[data-disclosure-fields]");
                if (fields) {
                    var willOpen = fields.classList.contains("hidden");
                    fields.classList.toggle("hidden", !willOpen);
                    disclosureForm.classList.toggle("is-expanded", willOpen);
                    if (willOpen) {
                        var firstField = fields.querySelector("input, textarea");
                        if (firstField) {
                            firstField.focus();
                        }
                    }
                }
            }
            return;
        }

        var cancelButton = event.target.closest("[data-disclosure-cancel]");
        if (cancelButton) {
            event.preventDefault();
            var cancelForm = cancelButton.closest("[data-disclosure-form]");
            if (cancelForm) {
                var cancelFields = cancelForm.querySelector("[data-disclosure-fields]");
                if (cancelFields) {
                    cancelFields.classList.add("hidden");
                }
                cancelForm.classList.remove("is-expanded");
            }
            return;
        }

        var switchButton = event.target.closest("[data-login-switch]");
        if (switchButton) {
            switchLoginPanel(switchButton.getAttribute("data-login-switch"));
            return;
        }

        var tab = event.target.closest("[data-login-tab]");
        if (tab) {
            switchLoginPanel(tab.getAttribute("data-login-tab"));
            return;
        }

        var button = event.target.closest("[data-sms-purpose]");
        if (!button) {
            return;
        }

        var phoneInput = document.getElementById(button.getAttribute("data-phone-input"));
        var result = document.getElementById("smsResult");
        var phone = phoneInput ? phoneInput.value.trim() : "";
        if (!phone) {
            if (result) {
                result.textContent = "请先输入电话";
            }
            return;
        }

        button.disabled = true;
        var form = new URLSearchParams();
        form.set("phone", phone);
        form.set("purpose", button.getAttribute("data-sms-purpose"));

        fetch(pageContext() + "/sms-code", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
            body: form.toString(),
            credentials: "same-origin"
        }).then(function (response) {
            return response.json();
        }).then(function (data) {
            if (handleForcedLogout(data)) {
                return;
            }
            if (result) {
                result.textContent = data.message;
            }
        }).catch(function () {
            if (result) {
                result.textContent = "验证码获取失败";
            }
        }).finally(function () {
            button.disabled = false;
        });
    });

    document.addEventListener("submit", function (event) {
        var form = event.target.closest("form.ajax-action");
        if (!form) {
            return;
        }

        event.preventDefault();
        var submitter = event.submitter;
        var body = new URLSearchParams(new FormData(form));
        if (submitter && submitter.name) {
            body.set(submitter.name, submitter.value);
        }

        if (submitter) {
            submitter.disabled = true;
        }

        var actionUrl = new URL(form.getAttribute("action"), window.location.href).toString();

        fetch(actionUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "fetch"
            },
            body: body.toString(),
            credentials: "same-origin"
        }).then(function (response) {
            return response.json();
        }).then(function (data) {
            if (handleForcedLogout(data)) {
                return;
            }
            showActionMessage(data.message, data.ok);
            if (data.ok) {
                updatePostStats(data.post);
                updateCommentStats(data.comment);

                var action = body.get("action");
                if (action === "report") {
                    var reason = form.querySelector("input[name='reason']");
                    if (reason) {
                        reason.value = "";
                    }
                    var reportFields = form.querySelector("[data-disclosure-fields]");
                    if (reportFields) {
                        reportFields.classList.add("hidden");
                        form.classList.remove("is-expanded");
                    }
                } else if (action === "edit" && form.classList.contains("comment-edit")) {
                    var textarea = form.querySelector("textarea[name='content']");
                    var comment = form.closest(".comment");
                    var content = comment ? comment.querySelector(".comment-content") : null;
                    if (textarea && content) {
                        content.textContent = textarea.value;
                    }
                    var editFields = form.querySelector("[data-disclosure-fields]");
                    if (editFields) {
                        editFields.classList.add("hidden");
                        form.classList.remove("is-expanded");
                    }
                }
            }
        }).catch(function () {
            showActionMessage("操作失败，请稍后再试", false);
        }).finally(function () {
            if (submitter) {
                submitter.disabled = false;
            }
        });
    });

    if (window.BBS_LOGGED_IN) {
        window.setTimeout(checkSessionStatus, 5000);
        window.setInterval(checkSessionStatus, 15000);
    }
})();
