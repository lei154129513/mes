package com.qcadoo.plugin.integration;

import org.w3c.dom.Node;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MockModuleFactory implements ModuleFactory<MockModule> {

    @Override
    public void postInitialize() {
    }

    @Override
    public MockModule parse(final String pluginIdentifier, final Node node) {
        return new MockModule();
    }

    @Override
    public String getIdentifier() {
        return "mock";
    }

}