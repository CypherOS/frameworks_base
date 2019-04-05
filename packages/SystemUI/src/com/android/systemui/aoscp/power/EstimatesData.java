/*
 * Copyright (C) 2019 CypherOS
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.systemui.aoscp.power;

public class EstimatesData {

    private int id;
    private String estimate;

    public EstimatesData(int id, String estimate) {
        this.id = id;
        this.estimate = estimate;
    }

    public int getID() {
        return this.id;
    }

    public String getEstimate() {
        return this.estimate;
    }
}
