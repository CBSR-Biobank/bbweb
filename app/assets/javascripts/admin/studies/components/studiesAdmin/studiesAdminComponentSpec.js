/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: studiesAdmin', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'factory');

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
    spyOn(this.Study, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));
    this.createController();
    expect(this.controller.breadcrumbs).toBeDefined();
  });

});
