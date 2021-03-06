/*
 * Copyright 2016-2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.gaffer.types.function;

import uk.gov.gchq.gaffer.types.FreqMap;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.function.KorypheFunction;

/**
 * A {@code ToFreqMap} is a {@link KorypheFunction} that
 * creates a new FreqMap and upserts a given value.
 */
@Since("1.8.0")
@Summary("Creates a new FreqMap and upserts a given value")
public class ToFreqMap extends KorypheFunction<Object, FreqMap> {
    @Override
    public FreqMap apply(final Object value) {
        return new FreqMap(null != value ? value.toString() : null);
    }
}
