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
package io.spring.batch.configuration;

import java.util.Collections;

import org.apache.geode.cache.FixedPartitionAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.PartitionAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.PdxWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.FixedPartitionAttributesFactoryBean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctions;

import io.spring.batch.domain.Item;
import io.spring.batch.geode.SortedFileWriterFunction;
import io.spring.batch.geode.SortingPartitionResolver;

/**
 * @author Michael Minella
 */
@PeerCacheApplication(name="SortClusterApplication", locators = "localhost[10334]")
@EnablePdx(serializerBeanName = "pdxSerializer")
@EnableGemfireFunctionExecutions(basePackageClasses = SortedFileWriterFunction.class)
@EnableGemfireFunctions
public class GeodeConfiguration {

	@Profile("locator-manager")
	@Configuration
	@EnableLocator
	@EnableManager(start = true)
	public static class LocatorManagerConfiguration { }

	@Bean("Items")
	public PartitionedRegionFactoryBean<byte[], Item> itemsRegion(GemFireCache gemfireCache,
			RegionAttributes<byte[], Item> regionAttributes) {

		PartitionedRegionFactoryBean<byte[], Item> itemsRegion = new PartitionedRegionFactoryBean<>();

		itemsRegion.setCache(gemfireCache);
		itemsRegion.setClose(false);
		itemsRegion.setPersistent(false);
		itemsRegion.setAttributes(regionAttributes);

		return itemsRegion;
	}

	@Bean
	public RegionAttributesFactoryBean itemsRegionAttributes(
			PartitionAttributes<byte[], Item> itemsPartitionAttributes) {

		RegionAttributesFactoryBean itemsRegionAttributes = new RegionAttributesFactoryBean();

		itemsRegionAttributes.setPartitionAttributes(itemsPartitionAttributes);

		return itemsRegionAttributes;
	}

	@Bean
	public PartitionAttributesFactoryBean<byte[], Item> itemsPartitionAttributes(
			FixedPartitionAttributes itemsFixedPartitionAttributes,
			SortingPartitionResolver partitionResolver) {

		PartitionAttributesFactoryBean<byte[], Item> itemsPartitionAttributes = new PartitionAttributesFactoryBean<>();

		itemsPartitionAttributes.setFixedPartitionAttributes(Collections.singletonList(itemsFixedPartitionAttributes));
		itemsPartitionAttributes.setPartitionResolver(partitionResolver);

		return itemsPartitionAttributes;
	}

	@Bean
	public FixedPartitionAttributesFactoryBean itemsFixedPartitionAttributes(
			@Value("${partition.name}") String partitionName) {

		System.out.println(">> partitionName = " + partitionName);

		FixedPartitionAttributesFactoryBean itemsFixedPartitionAttributes = new FixedPartitionAttributesFactoryBean();

		itemsFixedPartitionAttributes.setPartitionName(partitionName);
		itemsFixedPartitionAttributes.setPrimary(true);

		return itemsFixedPartitionAttributes;

	}


	@Bean
	public GemfireTemplate gemfireTemplate(Region<?,?> region) {
		return new GemfireTemplate(region);
	}

	@Bean
	public PdxSerializer pdxSerializer() {

		return new PdxSerializer() {

			@Override
			public boolean toData(Object item, PdxWriter pdxWriter) {
				pdxWriter.writeByteArray("key", ((Item) item).getKey());
				pdxWriter.writeByteArray("record", ((Item) item).getRecord());
				return true;
			}

			@Override
			public Object fromData(Class<?> clazz, PdxReader pdxReader) {
				return new Item(pdxReader.readByteArray("key"), pdxReader.readByteArray("record"));
			}
		};
	}
}
