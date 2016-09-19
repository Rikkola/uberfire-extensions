package org.uberfire.ext.security.server;

import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.uberfire.backend.server.security.RoleRegistry;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.uberfire.ext.security.server.ServletSecurityAuthenticationService.USER_SESSION_ATTR_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ServletSecurityAuthenticationServiceTest {

    private static final String USERNAME = "user1";
    private static final String PASSWORD = "password1";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession httpSession;

    private ServletSecurityAuthenticationService tested;

    @Before
    public void setup() throws Exception {

        Principal p1 = mock( Principal.class );
        when( p1.getName() ).thenReturn( USERNAME );
        doReturn( p1 ).when( request ).getUserPrincipal();
        doReturn( httpSession ).when( request ).getSession();
        doReturn( null ).when( httpSession ).getAttribute( eq( USER_SESSION_ATTR_NAME ) );
        when(request.getSession(anyBoolean())).then(new Answer<HttpSession>() {
            @Override
            public HttpSession answer(InvocationOnMock invocationOnMock) throws Throwable {
                return httpSession;
            }
        });

        tested = spy( new ServletSecurityAuthenticationService() );

        // Set the request in the thread context.
        SecurityIntegrationFilter.requests.set( request );

    }

    @Test
    public void testLoggedIn() {
        assertTrue( tested.isLoggedIn() );
    }


    @Test
    public void testNotLoggedIn() {
        doReturn( null ).when( request ).getUserPrincipal();
        assertFalse( tested.isLoggedIn() );
    }

    @Test
    public void testLogin() throws Exception {

        RoleRegistry.get().registerRole( "admin" );
        RoleRegistry.get().registerRole( "role1" );
        Set<Principal> principals = mockPrincipals( "admin", "role1", "group1" );
        Subject subject = new Subject();
        subject.getPrincipals().addAll( principals );
        doReturn( subject ).when( tested ).getSubjectFromPolicyContext();

        User user = tested.login( USERNAME, PASSWORD );

        assertNotNull( user );
        assertEquals( USERNAME, user.getIdentifier() );
        assertEquals( 2, user.getRoles().size() );
        assertTrue( user.getRoles().contains( new RoleImpl( "admin" ) ) );
        assertTrue( user.getRoles().contains( new RoleImpl( "role1" ) ) );
        assertEquals( 1, user.getGroups().size() );
        assertTrue( user.getGroups().contains( new GroupImpl( "group1" ) ) );
    }

    @Test
    public void testLoginSubjectGroups() throws Exception {
        String username = "user1";
        String password = "password1";
        RoleRegistry.get().registerRole( "admin" );
        RoleRegistry.get().registerRole( "role1" );
        Set<Principal> principals = mockPrincipals( "admin", "role1", "group1" );
        Group aclGroup = mock( Group.class );
        doReturn( ServletSecurityAuthenticationService.DEFAULT_ROLE_PRINCIPLE_NAME ).when( aclGroup ).getName();
        Set<Principal> aclGroups = mockPrincipals( "g1", "g2" );
        Enumeration<? extends Principal> aclGroupsEnum = Collections.enumeration( aclGroups );
        doReturn( aclGroupsEnum ).when( aclGroup ).members();
        Subject subject = new Subject();
        subject.getPrincipals().addAll( principals );
        subject.getPrincipals().add( aclGroup );
        doReturn( subject ).when( tested ).getSubjectFromPolicyContext();

        User user = tested.login( username, password );

        assertNotNull( user );
        assertEquals( username, user.getIdentifier() );
        assertEquals( 2, user.getRoles().size() );
        assertTrue( user.getRoles().contains( new RoleImpl( "admin" ) ) );
        assertTrue( user.getRoles().contains( new RoleImpl( "role1" ) ) );
        assertEquals( 3, user.getGroups().size() );
        assertTrue( user.getGroups().contains( new GroupImpl( "group1" ) ) );
        assertTrue( user.getGroups().contains( new GroupImpl( "g1" ) ) );
        assertTrue( user.getGroups().contains( new GroupImpl( "g2" ) ) );
    }

    @Test
    public void testLogout() throws Exception {
        tested.logout();
        verify( request, times( 1 ) ).logout();
        verify( httpSession, times( 1 ) ).invalidate();

    }

    private Set<Principal> mockPrincipals( String... names ) {
        Set<Principal> principals = new HashSet<Principal>();
        for ( String name : names ) {
            Principal p1 = mock( Principal.class );
            when( p1.getName() ).thenReturn( name );
            principals.add( p1 );
        }
        return principals;
    }

}
