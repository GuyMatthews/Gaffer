/*
 * Copyright 2016 Crown Copyright
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

package uk.gov.gchq.gaffer.graph.hook;

import com.google.common.collect.Sets;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import uk.gov.gchq.gaffer.commonutil.exception.UnauthorisedException;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.generate.GenerateObjects;
import uk.gov.gchq.gaffer.operation.impl.get.GetAdjacentIds;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.operation.impl.get.GetElements;
import uk.gov.gchq.gaffer.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


public class OperationAuthoriserTest extends GraphHookTest<OperationAuthoriser> {
    private static final String OP_AUTHS_PATH = "/opAuthoriser.json";

    public OperationAuthoriserTest() {
        super(OperationAuthoriser.class);
    }

    @Test
    public void shouldAcceptOperationChainWhenUserHasAllOpAuths() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new GetElements())
                .then(new GenerateObjects<>())
                .build();
        final User user = new User.Builder()
                .opAuths("SuperUser", "ReadUser", "User")
                .build();

        // When
        hook.preExecute(opChain, user);

        // Then - no exceptions
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAllOpAuthsForAllOperations() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new GetAdjacentIds())  // Requires SuperUser
                .build();

        final User user = new User.Builder()
                .opAuths("WriteUser", "ReadUser", "User")
                .build();

        // When/Then
        try {
            hook.preExecute(opChain, user);
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAnyOpAuths() throws OperationException {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new GetAdjacentIds())
                .then(new GetElements())
                .build();

        final User user = new User();

        // When/Then
        try {
            hook.preExecute(opChain, user);
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAllowedAuth() throws OperationException {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new GetAdjacentIds())
                .then(new GetElements())
                .build();

        final User user = new User.Builder()
                .opAuths("unknownAuth")
                .build();

        // When/Then
        try {
            hook.preExecute(opChain, user);
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldReturnAllOpAuths() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);

        // When
        final Set<String> allOpAuths = hook.getAllAuths();

        // Then
        assertThat(allOpAuths,
                IsCollectionContaining.hasItems("User", "ReadUser", "WriteUser", "SuperUser", "AdminUser"));
    }

    @Test
    public void shouldReturnResultWithoutModification() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final Object result = mock(Object.class);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new GenerateObjects<>())
                .build();
        final User user = new User.Builder()
                .opAuths("NoScore")
                .build();

        // When
        final Object returnedResult = hook.postExecute(result, opChain, user);

        // Then
        assertSame(result, returnedResult);
    }

    @Test
    public void shouldSetAndGetAuths() {
        // Given
        final OperationAuthoriser hook = new OperationAuthoriser();
        final Map<Class<?>, Set<String>> auths = new HashMap<>();
        auths.put(Operation.class, Sets.newHashSet("auth1"));
        auths.put(GetElements.class, Sets.newHashSet("auth2"));
        auths.put(GetAllElements.class, Sets.newHashSet("auth3", "auth4"));

        // When
        hook.setAuths(auths);
        final Map<Class<?>, Set<String>> result = hook.getAuths();

        // Then
        assertEquals(auths, result);
        assertEquals(
                Sets.newHashSet("auth1", "auth2", "auth3", "auth4"),
                hook.getAllAuths()
        );
    }

    @Test
    public void shouldSetAndGetAuthsAsStrings() throws ClassNotFoundException {
        // Given
        final OperationAuthoriser hook = new OperationAuthoriser();
        final Map<String, Set<String>> auths = new HashMap<>();
        auths.put(Operation.class.getName(), Sets.newHashSet("auth1"));
        auths.put(GetElements.class.getName(), Sets.newHashSet("auth2"));
        auths.put(GetAllElements.class.getName(), Sets.newHashSet("auth3", "auth4"));

        // When
        hook.setAuthsFromStrings(auths);
        final Map<String, Set<String>> result = hook.getAuthsAsStrings();

        // Then
        assertEquals(auths, result);
        assertEquals(
                Sets.newHashSet("auth1", "auth2", "auth3", "auth4"),
                hook.getAllAuths()
        );
    }

    @Test
    public void shouldHandleNestedOperationChain(){}

    @Override
    protected OperationAuthoriser getTestObject() {
        return fromJson(OP_AUTHS_PATH);
    }
}