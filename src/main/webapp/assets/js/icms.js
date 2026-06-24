/* ICMS — progressive enhancement only. The app works without JS; this adds
   tabs, multi-step forms, and confirm dialogs in later phases. */
(function () {
    "use strict";

    // Highlight the active sidebar link based on the current path.
    var path = window.location.pathname;
    document.querySelectorAll(".nav-item").forEach(function (a) {
        if (a.getAttribute("href") && path.indexOf(a.getAttribute("href")) === 0) {
            a.classList.add("active");
        }
    });

    // Generic confirm guard: any element with data-confirm asks before proceeding.
    document.addEventListener("click", function (e) {
        var el = e.target.closest("[data-confirm]");
        if (el && !window.confirm(el.getAttribute("data-confirm"))) {
            e.preventDefault();
        }
    });

    /* ---- Surveyor assessment: dynamic components + live net-payable ---- */
    var compBody = document.getElementById("compBody");
    if (compBody) {
        var num = function (id) {
            var el = document.getElementById(id);
            var v = el ? parseFloat(el.value) : 0;
            return isNaN(v) ? 0 : v;
        };
        var fmt = function (n) { return n.toFixed(2); };

        var recalc = function () {
            var gross = 0;
            compBody.querySelectorAll(".comp-cost").forEach(function (c) {
                var v = parseFloat(c.value);
                if (!isNaN(v)) { gross += v; }
            });
            var deprAmt = gross * num("deprPct") / 100;
            var net = gross - num("deductible") - deprAmt - num("salvage");
            if (net < 0) { net = 0; }
            document.getElementById("grossOut").textContent = fmt(gross);
            document.getElementById("deprOut").textContent = fmt(deprAmt);
            document.getElementById("netOut").textContent = fmt(net);
        };

        document.getElementById("addComp").addEventListener("click", function () {
            var row = compBody.querySelector(".comp-row").cloneNode(true);
            row.querySelectorAll("input").forEach(function (i) {
                i.value = i.classList.contains("comp-cost") ? "0" : "";
            });
            compBody.appendChild(row);
        });

        compBody.addEventListener("click", function (e) {
            if (e.target.classList.contains("remove-comp")) {
                if (compBody.querySelectorAll(".comp-row").length > 1) {
                    e.target.closest(".comp-row").remove();
                    recalc();
                }
            }
        });

        var form = document.getElementById("assessForm");
        form.addEventListener("input", recalc);
        recalc();
    }
})();
