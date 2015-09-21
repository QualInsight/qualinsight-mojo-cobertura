/*
 * This file is part of qualinsight-mojo-cobertura-core.
 *
 * qualinsight-mojo-cobertura-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qualinsight-mojo-cobertura-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with qualinsight-mojo-cobertura-core.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qualinsight.mojo.cobertura.core;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "instrument-it", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ITInstrumentationMojo extends AbstractInstrumentationMojo {

}
