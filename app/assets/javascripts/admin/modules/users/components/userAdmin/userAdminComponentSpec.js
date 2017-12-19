/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: userAdminComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'userService',
                              'User',
                              'UserCounts',
                              'Factory');

      /*
       * Have to create controller as a directive so that $onInit() is fired on the controller.
       */
      this.createController = (user) => {
        this.userService.requestCurrentUser =
          jasmine.createSpy().and.returnValue(this.$q.when(user));

        ComponentTestSuiteMixin.createController.call(
          this,
          '<user-admin></user-admin>',
          undefined,
          'userAdmin');
      };
    });
  });

  it('scope is valid on startup', function() {
    const user = this.User.create(this.Factory.user());
    this.createController(user);
    expect(this.controller.breadcrumbs).toBeDefined();
    expect(this.controller.user).toBe(user);
  });

});
