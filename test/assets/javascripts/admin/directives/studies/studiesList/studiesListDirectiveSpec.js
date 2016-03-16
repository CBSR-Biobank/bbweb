/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'underscore',
  'angularMocks'
], function(angular, _, mocks) {
  'use strict';

  describe('Directive: studiesListDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                = self.$injector.get('$q');
      self.Study             = self.$injector.get('Study');
      self.StudyCounts       = self.$injector.get('StudyCounts');
      self.jsonEntities      = self.$injector.get('jsonEntities');
      self.createStudyCounts = setupCountsCreator();
      self.createController  = setupController();

      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/studiesList/studiesList.html',
        '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');


      function setupCountsCreator() {
        return create;

        //--

        function create(disabled, enabled, retired) {
          return new self.StudyCounts({
            total:    disabled + enabled + retired,
            disabled: disabled,
            enabled:  enabled,
            retired:  retired
          });
        }
      }

      function setupController() {
        return create;

        //---

        function create(studyCounts) {
          self.element = angular.element('<studies-list></studies-list>');
          self.scope = $rootScope.$new();
          $compile(self.element)(self.scope);
          self.scope.$digest();
          self.controller = self.element.controller('studiesList');
        }
      }
    }));

    it('scope is valid on startup', function() {
      var self        = this,
          StudyStatus = this.$injector.get('StudyStatus'),
          allStatuses = StudyStatus.values(),
          counts      = this.createStudyCounts(1, 2, 3);

      spyOn(self.StudyCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      spyOn(self.Study, 'list').and.callFake(function () {
        return self.$q.when(self.jsonEntities.pagedResult([]));
      });

      self.createController();

      expect(self.controller.studyCounts).toEqual(counts);
      expect(self.controller.pageSize).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(self.controller.possibleStatuses)
          .toContain({ id: status, label: StudyStatus.label(status)});
      });
      expect(self.controller.possibleStatuses).toContain({ id: 'all', label: 'All'});
    });

    it('updateStudies retrieves new list of studies', function() {
      var self = this,
          counts = self.createStudyCounts(1, 2, 3),
          listOptions = { dummy: 'value' };

      spyOn(self.StudyCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      spyOn(self.Study, 'list').and.callFake(function () {
        return self.$q.when(self.jsonEntities.pagedResult([]));
      });

      self.createController(counts);
      self.controller.updateStudies(listOptions);
      self.scope.$digest();

      expect(self.Study.list).toHaveBeenCalledWith(listOptions);
    });

  });

});
