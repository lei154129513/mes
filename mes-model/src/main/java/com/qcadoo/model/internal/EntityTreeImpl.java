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

package com.qcadoo.model.internal;

import java.util.AbstractList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

public final class EntityTreeImpl extends AbstractList<Entity> implements EntityTree {

    private final DataDefinition dataDefinition;

    private final Long belongsToId;

    private final FieldDefinition joinFieldDefinition;

    private List<Entity> entities = null;

    private EntityTreeNodeImpl root = null;

    public EntityTreeImpl(final DataDefinition dataDefinition, final String joinFieldName, final Long belongsToId) {
        this.dataDefinition = dataDefinition;
        this.joinFieldDefinition = dataDefinition.getField(joinFieldName);
        this.belongsToId = belongsToId;

        if (this.belongsToId == null) {
            entities = Collections.emptyList();
        }
    }

    private void loadEntities() {
        if (entities == null) {
            entities = find().orderAscBy("priority").list().getEntities();

            Map<Long, EntityTreeNodeImpl> entitiesById = new LinkedHashMap<Long, EntityTreeNodeImpl>();

            for (Entity entity : entities) {
                entitiesById.put(entity.getId(), new EntityTreeNodeImpl(entity));
            }

            for (EntityTreeNodeImpl entity : entitiesById.values()) {
                Entity parent = entity.getBelongsToField("parent");

                if (parent == null) {
                    if (root != null) {
                        throw new IllegalStateException("Tree cannot have multiple roots");
                    }

                    root = entity;
                } else {
                    if (entitiesById.get(parent.getId()) == null) {
                        throw new IllegalStateException("Parent for tree node not found");
                    }

                    entitiesById.get(parent.getId()).addChild(entity);
                }
            }

            if (!entities.isEmpty() && root == null) {
                throw new IllegalStateException("Root for tree not found");
            }
        }
    }

    /* (non-Javadoc)
     * @see com.qcadoo.mes.model.EntityTree#find()
     */
    @Override
    public SearchCriteriaBuilder find() {
        return dataDefinition.find().restrictedWith(Restrictions.belongsTo(joinFieldDefinition, belongsToId));
    }

    @Override
    public Entity get(final int index) {
        if (entities == null) {
            loadEntities();
        }
        return entities.get(index);
    }

    @Override
    public int size() {
        if (entities == null) {
            loadEntities();
        }
        return entities.size();
    }

    /* (non-Javadoc)
     * @see com.qcadoo.mes.model.EntityTree#getRoot()
     */
    @Override
    public EntityTreeNodeImpl getRoot() {
        if (entities == null) {
            loadEntities();
        }
        return root;
    }

    @Override
    public String toString() {
        return "EntityTree[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "]["
                + joinFieldDefinition.getName() + "=" + belongsToId + "]";
    }

}