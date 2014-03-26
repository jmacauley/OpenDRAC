/*
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

// +----------------------------------------------------------------+
// | Array functions that are missing in IE 5.0                     |
// | Author: Cezary Tomczak [www.gosu.pl]                           |
// | Free for any use as long as all copyright messages are intact. |
// +----------------------------------------------------------------+

// Removes the last element from an array and returns that element.
if (!Array.prototype.pop) {
    Array.prototype.pop = function() {
        var last;
        if (this.length) {
            last = this[this.length - 1];
            this.length -= 1;
        }
        return last;
    };
}

// Adds one or more elements to the end of an array and returns the new length of the array.
if (!Array.prototype.push) {
    Array.prototype.push = function() {
        for (var i = 0; i < arguments.length; ++i) {
            this[this.length] = arguments[i];
        }
        return this.length;
    };
}

// Removes the first element from an array and returns that element.
if (!Array.prototype.shift) {
    Array.prototype.shift = function() {
        var first;
        if (this.length) {
            first = this[0];
            for (var i = 0; i < this.length - 1; ++i) {
                this[i] = this[i + 1];
            }
            this.length -= 1;
        }
        return first;
    };
}

// Adds one or more elements to the front of an array and returns the new length of the array.
if (!Array.prototype.unshift) {
    Array.prototype.unshift = function() {
        if (arguments.length) {
            var i, len = arguments.length;
            for (i = this.length + len - 1; i >= len; --i) {
                this[i] = this[i - len];
            }
            for (i = 0; i < len; ++i) {
                this[i] = arguments[i];
            }
        }
        return this.length;
    };
}

// Adds and/or removes elements from an array.
if (!Array.prototype.splice) {
    Array.prototype.splice = function(index, howMany) {
        var elements = [], removed = [], i;
        for (i = 2; i < arguments.length; ++i) {
            elements.push(arguments[i]);
        }
        for (i = index; (i < index + howMany) && (i < this.length); ++i) {
            removed.push(this[i]);
        }
        for (i = index + howMany; i < this.length; ++i) {
            this[i - howMany] = this[i];
        }
        this.length -= removed.length;
        for (i = this.length + elements.length - 1; i >= index + elements.length; --i) {
            this[i] = this[i - elements.length];
        }
        for (i = 0; i < elements.length; ++i) {
            this[index + i] = elements[i];
        }
        return removed;
    };
}