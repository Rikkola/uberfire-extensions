/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.ext.wires.bpmn.client.rules.impl;

import org.junit.Test;
import org.uberfire.ext.wires.bpmn.api.model.Content;
import org.uberfire.ext.wires.bpmn.api.model.impl.nodes.EndProcessNode;
import org.uberfire.ext.wires.bpmn.api.model.impl.nodes.ProcessNode;
import org.uberfire.ext.wires.bpmn.api.model.impl.nodes.StartProcessNode;
import org.uberfire.ext.wires.bpmn.api.model.rules.Rule;
import org.uberfire.ext.wires.bpmn.beliefs.graph.GraphNode;
import org.uberfire.ext.wires.bpmn.client.AbstractBaseRuleTest;
import org.uberfire.ext.wires.bpmn.client.TestDummyNode;
import org.uberfire.ext.wires.bpmn.client.commands.ResultType;
import org.uberfire.ext.wires.bpmn.client.commands.Results;
import org.uberfire.ext.wires.bpmn.client.rules.RuleManager;

import static junit.framework.Assert.*;

public class CardinalityRulesTest extends AbstractBaseRuleTest {

    @Test
    public void testAddStartProcessNodeToProcess() {
        final ProcessNode process = new ProcessNode();
        final StartProcessNode node = new StartProcessNode();
        final RuleManager ruleManager = new DefaultRuleManagerImpl();

        for ( Rule rule : getCardinalityRules() ) {
            ruleManager.addRule( rule );
        }

        //Try to add a single StartProcessNode
        final Results results1 = ruleManager.checkCardinality( process,
                                                               node );

        assertNotNull( results1 );
        assertEquals( 0,
                      results1.getMessages().size() );
        process.addNode( node );

        //Try to add a second StartProcessNode
        final Results results2 = ruleManager.checkCardinality( process,
                                                               node );

        assertNotNull( results2 );
        assertEquals( 1,
                      results2.getMessages().size() );
        assertEquals( 1,
                      results2.getMessages( ResultType.ERROR ).size() );
    }

    @Test
    public void testAddEndProcessNodeToProcess() {
        final ProcessNode process = new ProcessNode();
        final EndProcessNode node = new EndProcessNode();
        final RuleManager ruleManager = new DefaultRuleManagerImpl();

        for ( Rule rule : getCardinalityRules() ) {
            ruleManager.addRule( rule );
        }

        //Try to add a single EndProcessNode.
        final Results results1 = ruleManager.checkCardinality( process,
                                                               node );

        assertNotNull( results1 );
        assertEquals( 0,
                      results1.getMessages().size() );
        process.addNode( node );

        //Try to add a second EndProcessNode
        final Results results2 = ruleManager.checkCardinality( process,
                                                               node );

        assertNotNull( results2 );
        assertEquals( 1,
                      results2.getMessages().size() );
        assertEquals( 1,
                      results2.getMessages( ResultType.ERROR ).size() );
    }

    @Test
    public void testAddDummyNodeToProcess() {
        final ProcessNode process = new ProcessNode();
        final GraphNode<Content> node = new TestDummyNode();
        final RuleManager ruleManager = new DefaultRuleManagerImpl();

        for ( Rule rule : getCardinalityRules() ) {
            ruleManager.addRule( rule );
        }

        //Try to add a single TestDummyNode. There are no rules restricting the cardinality of TestDummyNode.
        final Results results = ruleManager.checkCardinality( process,
                                                              node );

        assertNotNull( results );
        assertEquals( 0,
                      results.getMessages().size() );
    }

}