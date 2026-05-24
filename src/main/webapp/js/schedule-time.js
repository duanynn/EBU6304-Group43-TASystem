(function () {
    var MIN = '08:00';
    var MAX = '23:00';

    function pad(n) {
        return (n < 10 ? '0' : '') + n;
    }

    function toMinutes(value) {
        if (!value) return 0;
        var parts = value.split(':');
        return parseInt(parts[0], 10) * 60 + parseInt(parts[1], 10);
    }

    function fromMinutes(m) {
        m = Math.max(toMinutes(MIN), Math.min(toMinutes(MAX), m));
        var h = Math.floor(m / 60);
        var min = m % 60;
        return pad(h) + ':' + pad(min);
    }

    function findStartEnd(row) {
        var start = row.querySelector(
            'input[type="time"][name="slotStart"], input[type="time"][name="availStart"], input[type="time"][name="interviewStart"]'
        );
        var end = row.querySelector(
            'input[type="time"][name="slotEnd"], input[type="time"][name="availEnd"], input[type="time"][name="interviewEnd"]'
        );
        return { start: start, end: end };
    }

    function applyBounds(input) {
        if (!input) return;
        input.min = MIN;
        input.max = MAX;
    }

    function syncEndAfterStart(pair) {
        if (!pair.start || !pair.end) return;
        var s = toMinutes(pair.start.value);
        var e = toMinutes(pair.end.value);
        if (e <= s) {
            var bumped = Math.min(toMinutes(MAX), s + 60);
            if (bumped <= s) {
                bumped = Math.min(toMinutes(MAX), s + 30);
            }
            pair.end.value = fromMinutes(bumped);
        }
        pair.end.min = pair.start.value || MIN;
    }

    function bindRow(row) {
        var pair = findStartEnd(row);
        applyBounds(pair.start);
        applyBounds(pair.end);
        syncEndAfterStart(pair);
        if (pair.start) {
            pair.start.addEventListener('change', function () {
                syncEndAfterStart(pair);
            });
        }
        if (pair.end) {
            pair.end.addEventListener('change', function () {
                syncEndAfterStart(pair);
            });
        }
    }

    function bindAll(root) {
        var scope = root || document;
        scope.querySelectorAll('.schedule-slot-row').forEach(bindRow);
        scope.querySelectorAll('input[type="time"]').forEach(applyBounds);
    }

    function validateForm(form) {
        var ok = true;
        form.querySelectorAll('.schedule-slot-row').forEach(function (row) {
            var pair = findStartEnd(row);
            if (!pair.start || !pair.end) return;
            var s = toMinutes(pair.start.value);
            var e = toMinutes(pair.end.value);
            if (s < toMinutes(MIN) || e > toMinutes(MAX)) {
                ok = false;
            }
            if (e <= s) {
                ok = false;
                syncEndAfterStart(pair);
            }
        });
        return ok;
    }

    document.addEventListener('DOMContentLoaded', function () {
        bindAll(document);
        document.querySelectorAll('form').forEach(function (form) {
            if (!form.querySelector('.schedule-slot-row') && !form.querySelector('input[name="interviewStart"]')) {
                return;
            }
            form.addEventListener('submit', function (e) {
                bindAll(form);
                if (!validateForm(form)) {
                    e.preventDefault();
                    alert('Each time slot must be between 08:00 and 23:00, with start time earlier than end time.');
                }
            });
        });
    });

    window.ScheduleTimeUtil = {
        bindAll: bindAll,
        bindRow: bindRow,
        MIN: MIN,
        MAX: MAX
    };
})();
