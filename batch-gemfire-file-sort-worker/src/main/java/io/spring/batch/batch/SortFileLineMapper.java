/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.spring.batch.batch;

import java.util.Arrays;

import io.spring.batch.domain.Item;

/**
 * @author Michael Minella
 */
public class SortFileLineMapper {

	public Item mapLine(byte[] line, int lineNumber) throws Exception {
		byte [] key = Arrays.copyOfRange(line, 0, 10);

		return new Item(key, line);
	}
}
