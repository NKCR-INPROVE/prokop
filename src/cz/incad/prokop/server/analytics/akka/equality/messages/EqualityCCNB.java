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
package cz.incad.prokop.server.analytics.akka.equality.messages;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class EqualityCCNB {
    
    private String ccnb;
    
    public EqualityCCNB(String ccnb) {
        this.ccnb = ccnb;
    }

    public String getCcnb() {
        return ccnb;
    }
    
}
