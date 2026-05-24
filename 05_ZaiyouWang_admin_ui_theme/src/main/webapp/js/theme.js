(function () {
    var THEMES = ['default', 'eye', 'night'];

    function storageKey(userId) {
        return 'tasystem.theme.' + (userId || 'guest');
    }

    function applyTheme(themeId) {
        var t = THEMES.indexOf(themeId) >= 0 ? themeId : 'default';
        if (t === 'default') {
            document.documentElement.removeAttribute('data-theme');
        } else {
            document.documentElement.setAttribute('data-theme', t);
        }
        return t;
    }

    function loadTheme(userId) {
        var saved = localStorage.getItem(storageKey(userId));
        return applyTheme(saved || 'default');
    }

    function saveTheme(userId, themeId) {
        localStorage.setItem(storageKey(userId), applyTheme(themeId));
        syncRadios(themeId);
    }

    function syncRadios(themeId) {
        document.querySelectorAll('input[name="themeChoice"]').forEach(function (radio) {
            radio.checked = radio.value === themeId;
        });
    }

    function initFromDom() {
        var menu = document.querySelector('.account-menu');
        var userId = menu ? menu.getAttribute('data-user-id') : '';
        if (!userId) {
            userId = document.body.getAttribute('data-user-id') || '';
        }
        var current = loadTheme(userId);
        syncRadios(current);
        document.querySelectorAll('input[name="themeChoice"]').forEach(function (radio) {
            radio.addEventListener('change', function () {
                if (radio.checked) {
                    saveTheme(userId, radio.value);
                }
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initFromDom);
    } else {
        initFromDom();
    }

    window.TaTheme = {
        apply: applyTheme,
        load: loadTheme,
        save: saveTheme
    };
})();
