/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: studiesAdmin', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'Factory');

      this.createController = () => {
        this.element = angular.element('<studies-admin></studies-admin>');
        this.scope = this.$rootScope.$new();
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('studiesAdmin');
      };
    });
  });

  it('scope is valid on startup', function() {
    spyOn(this.StudyCounts, 'get').and.returnValue(this.$q.when({
      total: 0,
      disabled: 0,
      enabled: 0,
      retired: 0
    }));
    spyOn(this.Study, 'list').and.returnValue(this.$q.when(this.Factory.pagedResult([])));
    this.createController();
    expect(this.controller.breadcrumbs).toBeDefined();
  });

});
