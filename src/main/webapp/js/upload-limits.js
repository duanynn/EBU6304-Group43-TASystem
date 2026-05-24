(function (global) {
    var CV_MAX = 5 * 1024 * 1024;
    var AVATAR_MAX = 2 * 1024 * 1024;

    function formatMb(bytes) {
        return (bytes / (1024 * 1024)).toFixed(0);
    }

    function checkFileSize(file, maxBytes, label) {
        if (!file) {
            return true;
        }
        if (file.size <= maxBytes) {
            return true;
        }
        window.alert(label + ' is too large (' + formatMb(file.size) + ' MB). Maximum allowed is ' + formatMb(maxBytes) + ' MB.');
        return false;
    }

    global.UploadLimitsClient = {
        CV_MAX: CV_MAX,
        AVATAR_MAX: AVATAR_MAX,
        checkCv: function (file) {
            return checkFileSize(file, CV_MAX, 'CV file');
        },
        checkAvatar: function (file) {
            return checkFileSize(file, AVATAR_MAX, 'Profile photo');
        }
    };
})(window);
