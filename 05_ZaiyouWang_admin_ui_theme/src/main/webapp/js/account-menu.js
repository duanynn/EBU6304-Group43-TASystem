(function () {
    function closePanel(panel) {
        if (panel) panel.hidden = true;
        document.querySelectorAll('.account-menu-trigger').forEach(function (btn) {
            btn.setAttribute('aria-expanded', 'false');
        });
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
            var modal = document.getElementById('changePasswordModal');
            if (modal) modal.hidden = false;
            document.querySelectorAll('.account-menu-panel').forEach(function (p) {
                p.hidden = true;
            });
        });
    });

    document.querySelectorAll('[data-close-modal]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modal = document.getElementById('changePasswordModal');
            if (modal) modal.hidden = true;
        });
    });

    var modal = document.getElementById('changePasswordModal');
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                modal.hidden = true;
            }
        });
    }
})();
