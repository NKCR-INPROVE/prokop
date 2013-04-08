/*
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package cz.incad.prokop.server.analytics.akka.missing.messages;

import java.util.List;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class CountCNBResult {
    
    private List<String> counts;

    public CountCNBResult(List<String> counts) {
        this.counts = counts;
    }

    public List<String> getCount() {
        return this.counts;
    }
}
