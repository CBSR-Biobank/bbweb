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

    var createStudyCounts = function (disabled, enabled, retired) {
      return new this.StudyCounts({
        total:    disabled + enabled + retired,
        disabled: disabled,
        enabled:  enabled,
        retired:  retired
      });
    };

    var createController = function (studyCounts) {
      this.element = angular.element('<studies-list></studies-list>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studiesList');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Study',
                              'StudyCounts',
                              'factory');

      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/studiesList/studiesList.html',
        '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');

      //---
    }));

    it('scope is valid on startup', function() {
      var self             = this,
          StudyStatus      = this.$injector.get('StudyStatus'),
          studyStatusLabel = this.$injector.get('studyStatusLabel'),
          allStatuses      = _.values(StudyStatus),
          counts           = createStudyCounts.call(this, 1, 2, 3);

      spyOn(self.StudyCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      spyOn(self.Study, 'list').and.callFake(function () {
        return self.$q.when(self.factory.pagedResult([]));
      });

      createController.call(self);

      expect(self.controller.studyCounts).toEqual(counts);
      expect(self.controller.pageSize).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(self.controller.possibleStatuses)
          .toContain({ id: status, label: studyStatusLabel.statusToLabel(status)});
      });
      expect(self.controller.possibleStatuses).toContain({ id: 'all', label: 'All'});
    });

    it('updateStudies retrieves new list of studies', function() {
      var self = this,
          counts = createStudyCounts.call(this, 1, 2, 3),
          listOptions = { dummy: 'value' };

      spyOn(self.StudyCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      spyOn(self.Study, 'list').and.callFake(function () {
        return self.$q.when(self.factory.pagedResult([]));
      });

      createController.call(self, counts);
      self.controller.updateStudies(listOptions);
      self.scope.$digest();

      expect(self.Study.list).toHaveBeenCalledWith(listOptions);
    });

  });

});
