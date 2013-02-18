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

package com.github.fge.jsonschema.jsonpointer;

import com.fasterxml.jackson.core.TreeNode;
import com.github.fge.jsonschema.exceptions.JsonReferenceException;
import com.github.fge.jsonschema.exceptions.unchecked.JsonReferenceError;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;

import static com.github.fge.jsonschema.matchers.ProcessingMessageAssert.*;
import static com.github.fge.jsonschema.messages.JsonReferenceMessages.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class TreePointerTest
{
    @Test
    public void attemptToBuildTokensFromNullRaisesAnError()
        throws JsonReferenceException
    {
        try {
            TreePointer.tokensFromInput(null);
            fail("No exception thrown!!");
        } catch (JsonReferenceError e) {
            final ProcessingMessage message = e.getProcessingMessage();
            assertMessage(message).hasMessage(NULL_INPUT);
        }
    }

    @Test
    public void buildingTokenListYellsIfIllegalPointer()
    {
        try {
            TreePointer.tokensFromInput("a/b");
            fail("No exception thrown!!");
        } catch (JsonReferenceException e) {
            final ProcessingMessage message = e.getProcessingMessage();
            assertMessage(message).hasMessage(NOT_SLASH)
                .hasField("expected", '/').hasField("found", 'a');
        }
    }

    @Test
    public void buildingTokenListIsUnfazedByAnEmptyInput()
        throws JsonReferenceException
    {
        assertEquals(TreePointer.tokensFromInput(""),
            ImmutableList.<ReferenceToken>of());
    }

    @Test
    public void buildingTokenListIsUnfazedByEmptyToken()
        throws JsonReferenceException
    {
        final List<ReferenceToken> expected
            = ImmutableList.of(ReferenceToken.fromCooked(""));
        final List<ReferenceToken> actual = TreePointer.tokensFromInput("/");

        assertEquals(actual, expected);
    }

    @Test
    public void tokenListRespectsOrder()
        throws JsonReferenceException
    {
        final List<ReferenceToken> expected = ImmutableList.of(
            ReferenceToken.fromRaw("/"),
            ReferenceToken.fromRaw("~"),
            ReferenceToken.fromRaw("x")
        );
        final List<ReferenceToken> actual
            = TreePointer.tokensFromInput("/~1/~0/x");

        assertEquals(actual, expected);
    }

    @Test
    public void tokenListAccountsForEmptyTokens()
        throws JsonReferenceException
    {
        final List<ReferenceToken> expected = ImmutableList.of(
            ReferenceToken.fromRaw("a"),
            ReferenceToken.fromRaw(""),
            ReferenceToken.fromRaw("b")
        );
        final List<ReferenceToken> actual
            = TreePointer.tokensFromInput("/a//b");

        assertEquals(actual, expected);
    }

    @Test
    public void gettingTraversalResultGoesNoFurtherThanFirstMissing()
    {
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token1 = mock(TokenResolver.class);
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token2 = mock(TokenResolver.class);
        final TreeNode missing = mock(TreeNode.class);

        when(token1.get(any(TreeNode.class))).thenReturn(null);

        final DummyPointer ptr = new DummyPointer(missing,
            ImmutableList.of(token1, token2));

        final TreeNode node = mock(TreeNode.class);
        final TreeNode ret = ptr.get(node);
        verify(token1, only()).get(node);
        verify(token2, never()).get(any(TreeNode.class));

        assertNull(ret);
    }

    @Test
    public void gettingPathOfMissingNodeReturnsMissingNode()
    {
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token1 = mock(TokenResolver.class);
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token2 = mock(TokenResolver.class);
        final TreeNode missing = mock(TreeNode.class);

        when(token1.get(any(TreeNode.class))).thenReturn(null);

        final DummyPointer ptr = new DummyPointer(missing,
            ImmutableList.of(token1, token2));

        final TreeNode node = mock(TreeNode.class);
        final TreeNode ret = ptr.path(node);
        verify(token1, only()).get(node);
        verify(token2, never()).get(any(TreeNode.class));

        assertSame(ret, missing);
    }

    @Test
    public void treePointerCanTellWhetherItIsEmpty()
    {
        final List<TokenResolver<TreeNode>> list = Lists.newArrayList();

        assertTrue(new DummyPointer(null, list).isEmpty());

        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> mock = mock(TokenResolver.class);

        list.add(mock);
        assertFalse(new DummyPointer(null, list).isEmpty());
    }

    @Test
    public void treeIsUnalteredWhenOriginalListIsAltered()
    {
        final List<TokenResolver<TreeNode>> list = Lists.newArrayList();
        final DummyPointer dummy = new DummyPointer(null, list);

        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> mock = mock(TokenResolver.class);
        list.add(mock);

        assertTrue(dummy.isEmpty());
    }

    private static final class DummyPointer
        extends TreePointer<TreeNode>
    {
        DummyPointer(final TreeNode missing,
            final List<TokenResolver<TreeNode>> tokenResolvers)
        {
            super(missing, tokenResolvers);
        }
    }
}
