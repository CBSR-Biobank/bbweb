/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'lodash',
  'angularMocks',
  'biobankApp'
], function(angular, _, mocks) {
  'use strict';

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function () {
      this.element = angular.element('<studies-admin></studies-admin>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studiesAdmin');
    };

    return SuiteMixin;
  }

  fdescribe('Component: studiesAdmin', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studiesAdmin/studiesAdmin.html',
        '/assets/javascripts/admin/studies/components/studiesPagedList/studiesPagedList.html',
        '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
    }));

    it('scope is valid on startup', function() {
      spyOn(this.StudyCounts, 'get').and.returnValue(this.$q.when({
        total: 0,
        disabled: 0,
        enabled: 0,
        retired: 0
      }));
      spyOn(this.Study, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));
      this.createScope();
      expect(this.controller.breadcrumbs).toBeDefined();
    });

  });

});
