/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.exceptions.JsonPatchException;
import com.github.fge.jsonschema.jsonpointer.JsonPointer;

public final class MoveOperation
    extends DualPathOperation
{
    @JsonCreator
    public MoveOperation(@JsonProperty("from") final JsonPointer from,
        @JsonProperty("path") final JsonPointer path)
    {
        super(from, path);
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        return node;
    }

    @Override
    public String toString()
    {
        return "move: " + super.toString();
    }
}