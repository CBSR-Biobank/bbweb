/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: centresAdmin', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Centre',
                              'CentreCounts',
                              'Factory');

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<centres-admin></centres-admin>',
          undefined,
          'centresAdmin');
    });
  });

  it('scope is valid on startup', function() {
    spyOn(this.CentreCounts, 'get').and.returnValue(this.$q.when({ total: 0, disabled: 0, enabled: 0 }));
    spyOn(this.Centre, 'list').and.returnValue(this.$q.when(this.Factory.pagedResult([])));
    this.createController();
    expect(this.scope).toBeDefined();
  });

});
