(function () {
    function closePanel(panel) {
        if (panel) panel.hidden = true;
        document.querySelectorAll('.account-menu-trigger').forEach(function (btn) {
            btn.setAttribute('aria-expanded', 'false');
        });
    }

    function getModal() {
        return document.getElementById('changePasswordModal');
    }

    function getModalAlert() {
        return document.getElementById('changePasswordModalAlert');
    }

    function showModalAlert(message, type) {
        var alertBox = getModalAlert();
        if (!alertBox) return;
        alertBox.textContent = message;
        alertBox.className = 'change-password-modal-alert alert ' +
            (type === 'success' ? 'alert-success' : (type === 'error' ? 'alert-error' : 'alert-info'));
        alertBox.hidden = false;
    }

    function hideModalAlert() {
        var alertBox = getModalAlert();
        if (!alertBox) return;
        alertBox.hidden = true;
        alertBox.textContent = '';
    }

    function showPageToast(message, type) {
        document.querySelectorAll('.account-page-toast').forEach(function (el) {
            el.remove();
        });
        var css = type === 'error' ? 'alert alert-error' : (type === 'success' ? 'alert alert-success' : 'alert alert-info');
        var box = document.createElement('div');
        box.className = css + ' account-flash-banner account-page-toast';
        box.setAttribute('role', 'status');
        box.textContent = message;
        var main = document.querySelector('.app-main');
        if (main) {
            main.insertBefore(box, main.firstChild);
        } else {
            document.body.insertBefore(box, document.body.firstChild);
        }
    }

    function openChangePasswordModal(clearCurrentOnly) {
        var modal = getModal();
        if (!modal) return;
        modal.hidden = false;
        document.querySelectorAll('.account-menu-panel').forEach(function (p) {
            p.hidden = true;
        });
        var form = document.getElementById('changePasswordForm');
        if (!form) return;
        var oldInput = form.querySelector('[name="oldPassword"]');
        if (oldInput) {
            oldInput.value = '';
            setTimeout(function () { oldInput.focus(); }, 0);
        }
    }

    function handleChangePasswordFlash() {
        var payload = document.getElementById('changePasswordFlashPayload');
        if (!payload) return;
        var message = payload.getAttribute('data-message');
        if (!message) {
            payload.remove();
            return;
        }
        var type = payload.getAttribute('data-type') || 'error';
        var shouldOpen = payload.getAttribute('data-open') === 'true';
        if (shouldOpen) {
            openChangePasswordModal(true);
        }
        showModalAlert(message, type);
        payload.remove();
    }

    document.addEventListener('click', function (e) {
        var trigger = e.target.closest('.account-menu-trigger');
        if (trigger) {
            e.preventDefault();
            var menu = trigger.closest('.account-menu');
            var panel = menu ? menu.querySelector('.account-menu-panel') : null;
            if (panel) {
                var open = panel.hidden;
                document.querySelectorAll('.account-menu-panel').forEach(function (p) {
                    p.hidden = true;
                });
                panel.hidden = !open;
                trigger.setAttribute('aria-expanded', open ? 'true' : 'false');
            }
            return;
        }
        if (!e.target.closest('.account-menu')) {
            document.querySelectorAll('.account-menu-panel').forEach(function (p) {
                p.hidden = true;
            });
        }
    });

    document.querySelectorAll('[data-action="change-password"]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            hideModalAlert();
            var modal = getModal();
            if (modal) modal.hidden = false;
            document.querySelectorAll('.account-menu-panel').forEach(function (p) {
                p.hidden = true;
            });
            var form = document.getElementById('changePasswordForm');
            if (form) {
                var oldInput = form.querySelector('[name="oldPassword"]');
                if (oldInput) oldInput.focus();
            }
        });
    });

    document.querySelectorAll('[data-close-modal]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modal = getModal();
            if (modal) modal.hidden = true;
            hideModalAlert();
        });
    });

    var modal = getModal();
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                modal.hidden = true;
                hideModalAlert();
            }
        });
    }

    var changePasswordForm = document.getElementById('changePasswordForm');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', function (e) {
            e.preventDefault();

            var oldInput = changePasswordForm.querySelector('[name="oldPassword"]');
            var newInput = changePasswordForm.querySelector('[name="newPassword"]');
            var confirmInput = changePasswordForm.querySelector('[name="confirmPassword"]');
            var oldVal = oldInput ? oldInput.value.trim() : '';
            var newVal = newInput ? newInput.value.trim() : '';
            var confirmVal = confirmInput ? confirmInput.value.trim() : '';

            if (!oldVal || !newVal || !confirmVal) {
                showModalAlert('All password fields are required.', 'error');
                if (oldInput && !oldVal) oldInput.focus();
                return;
            }
            if (newVal !== confirmVal) {
                showModalAlert('New passwords do not match.', 'error');
                if (confirmInput) confirmInput.focus();
                return;
            }
            if (oldVal === newVal) {
                showModalAlert('New password must be different from your current password.', 'error');
                if (newInput) newInput.focus();
                return;
            }

            var submitBtn = changePasswordForm.querySelector('button[type="submit"]');
            if (submitBtn) submitBtn.disabled = true;

            var returnUrlInput = changePasswordForm.querySelector('[name="returnUrl"]');
            var params = new URLSearchParams();
            params.set('oldPassword', oldVal);
            params.set('newPassword', newVal);
            params.set('confirmPassword', confirmVal);
            params.set('ajax', '1');
            if (returnUrlInput && returnUrlInput.value) {
                params.set('returnUrl', returnUrlInput.value);
            }

            fetch(changePasswordForm.action, {
                method: 'POST',
                body: params,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                credentials: 'same-origin'
            })
                .then(function (response) {
                    return response.json().catch(function () {
                        return { success: false, message: 'Unexpected server response.', type: 'error' };
                    });
                })
                .then(function (data) {
                    if (data && data.success) {
                        hideModalAlert();
                        var modalEl = getModal();
                        if (modalEl) modalEl.hidden = true;
                        changePasswordForm.reset();
                        showPageToast(data.message || 'Password updated successfully.', 'success');
                        return;
                    }
                    showModalAlert((data && data.message) || 'Could not update password.', 'error');
                    if (oldInput) {
                        oldInput.value = '';
                        oldInput.focus();
                    }
                })
                .catch(function () {
                    showModalAlert('Network error. Please try again.', 'error');
                    if (oldInput) {
                        oldInput.value = '';
                        oldInput.focus();
                    }
                })
                .finally(function () {
                    if (submitBtn) submitBtn.disabled = false;
                });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', handleChangePasswordFlash);
    } else {
        handleChangePasswordFlash();
    }
})();
