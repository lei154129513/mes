/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.internal;

import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.hibernate.criterion.Projections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.qcadoo.mes.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleTreeDatabaseObject;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.api.types.TreeType;
import com.qcadoo.model.internal.api.FieldHookDefinition;
import com.qcadoo.model.internal.types.HasManyEntitiesType;
import com.qcadoo.model.internal.types.TreeEntitiesType;
import com.qcadoo.model.internal.validators.UniqueValidator;

public final class DataAccessServiceCopyTest extends DataAccessTest {

    @Test
    public void shouldCopyEntity() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);

        // then
        verify(session, times(1)).save(Mockito.any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        Assert.assertEquals(66, entity.getField("age"));
        Assert.assertEquals("Mr T", entity.getField("name"));
    }

    @Test
    public void shouldCopyEntityWithUniqueField() throws Exception {
        // given
        FieldHookDefinition fieldHook = new UniqueValidator();
        fieldHook.initialize(dataDefinition, fieldDefinitionName);

        fieldDefinitionName.withValidator(fieldHook);

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(0);
        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);

        // then
        verify(session, times(1)).save(Mockito.any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        Assert.assertEquals(66, entity.getField("age"));
        Assert.assertEquals("Mr T(1)", entity.getField("name"));
    }

    @Test
    public void shouldCopyEntityWithUniqueField2() throws Exception {
        // given
        FieldHookDefinition fieldHook = new UniqueValidator();
        fieldHook.initialize(dataDefinition, fieldDefinitionName);

        fieldDefinitionName.withValidator(fieldHook);

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T(1)");
        simpleDatabaseObject.setAge(66);

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1, 0);
        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);

        // then
        verify(session, times(1)).save(Mockito.any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        Assert.assertEquals(66, entity.getField("age"));
        Assert.assertEquals("Mr T(3)", entity.getField("name"));
    }

    @Test
    public void shouldCopyEntityWithoutHasManyField() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(12L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        SampleParentDatabaseObject parentDatabaseObject = new SampleParentDatabaseObject();
        parentDatabaseObject.setId(13L);
        parentDatabaseObject.setName("Mr T");

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1, 0);
        given(session.get(Mockito.eq(SampleSimpleDatabaseObject.class), Mockito.eq(12L))).willReturn(simpleDatabaseObject);
        given(session.get(Mockito.eq(SampleParentDatabaseObject.class), Mockito.eq(13L))).willReturn(parentDatabaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList(simpleDatabaseObject));

        // when
        Entity entity = parentDataDefinition.copy(13L);

        // then
        Assert.assertEquals("Mr T", entity.getField("name"));
        assertTrue(entity.isValid());
        verify(session, times(1)).save(Mockito.any());
        verify(session, never()).get(Mockito.eq(SampleSimpleDatabaseObject.class), anyInt());
    }

    @Test
    public void shouldCopyEntityWithHasManyField() throws Exception {
        // given
        parentFieldDefinitionHasMany.withType(new HasManyEntitiesType("simple", "entity", "belongsTo",
                HasManyType.Cascade.DELETE, true, dataDefinitionService));

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(12L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        SampleParentDatabaseObject parentDatabaseObject = new SampleParentDatabaseObject();
        parentDatabaseObject.setId(13L);
        parentDatabaseObject.setName("Mr T");

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1, 0);
        given(session.get(Mockito.eq(SampleSimpleDatabaseObject.class), Mockito.eq(12L))).willReturn(simpleDatabaseObject);
        given(session.get(Mockito.eq(SampleParentDatabaseObject.class), Mockito.eq(13L))).willReturn(parentDatabaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList(simpleDatabaseObject));

        // when
        Entity entity = parentDataDefinition.copy(13L);

        // then
        Assert.assertEquals("Mr T", entity.getField("name"));
        assertTrue(entity.isValid());
        verify(session, times(2)).save(Mockito.any());
        verify(session, never()).get(Mockito.eq(SampleSimpleDatabaseObject.class), anyInt());
    }

    @Test
    public void shouldCopyEntityWithoutTreeField() throws Exception {
        // given
        SampleTreeDatabaseObject treeDatabaseObject = new SampleTreeDatabaseObject();
        treeDatabaseObject.setId(12L);
        treeDatabaseObject.setName("Mr T");

        SampleParentDatabaseObject parentDatabaseObject = new SampleParentDatabaseObject();
        parentDatabaseObject.setId(13L);
        parentDatabaseObject.setName("Mr T");

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1, 0);
        given(session.get(Mockito.eq(SampleSimpleDatabaseObject.class), Mockito.eq(12L))).willReturn(treeDatabaseObject);
        given(session.get(Mockito.eq(SampleParentDatabaseObject.class), Mockito.eq(13L))).willReturn(parentDatabaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList(treeDatabaseObject));

        // when
        Entity entity = parentDataDefinition.copy(13L);

        // then
        Assert.assertEquals("Mr T", entity.getField("name"));
        assertTrue(entity.isValid());
        verify(session, times(1)).save(Mockito.any());
        verify(session, never()).get(Mockito.eq(SampleSimpleDatabaseObject.class), anyInt());
    }

    @Test
    public void shouldCopyEntityWithTreeField() throws Exception {
        // given
        parentFieldDefinitionTree.withType(new TreeEntitiesType("tree", "entity", "owner", TreeType.Cascade.DELETE, true,
                dataDefinitionService));

        SampleTreeDatabaseObject treeDatabaseObject = new SampleTreeDatabaseObject();
        treeDatabaseObject.setId(12L);
        treeDatabaseObject.setName("Mr T");

        SampleParentDatabaseObject parentDatabaseObject = new SampleParentDatabaseObject();
        parentDatabaseObject.setId(13L);
        parentDatabaseObject.setName("Mr T");

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1, 0);
        given(session.get(Mockito.eq(SampleSimpleDatabaseObject.class), Mockito.eq(12L))).willReturn(treeDatabaseObject);
        given(session.get(Mockito.eq(SampleParentDatabaseObject.class), Mockito.eq(13L))).willReturn(parentDatabaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList(treeDatabaseObject));

        // when
        Entity entity = parentDataDefinition.copy(13L);

        // then
        Assert.assertEquals("Mr T", entity.getField("name"));
        assertTrue(entity.isValid());
        verify(session, times(2)).save(Mockito.any());
        verify(session, never()).get(Mockito.eq(SampleSimpleDatabaseObject.class), anyInt());
    }

}