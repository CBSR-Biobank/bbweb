/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('collectionDirective', function() {

    var createDirective = function (studyCounts, centreCounts) {
      studyCounts = studyCounts || getStudyCounts();
      centreCounts = centreCounts || getCentreCounts();

      this.element = angular.element([
        '<collection',
        '  study-counts="vm.studyCounts"',
        '  centre-counts="vm.centreCounts">',
        '</collection>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        studyCounts: studyCounts,
        centreCounts: centreCounts
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('collection');
    };

    function getStudyCounts() {
      return {
        disabled: faker.random.number(),
        enabled: faker.random.number(),
        retired: faker.random.number()
      };
    }

    function getCentreCounts() {
      return {
        disabled: faker.random.number(),
        enabled: faker.random.number()
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/directives/collection/collection.html',
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');
    }));

    it('has valid scope', function() {
      var studyCounts = getStudyCounts(),
          centreCounts = getCentreCounts();

      spyOn(this.Study, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));

      createDirective.call(this, studyCounts, centreCounts);
      expect(this.controller.studyCounts).toBe(studyCounts);
      expect(this.controller.centreCounts).toBe(centreCounts);
      expect(this.controller.haveEnabledStudies).toBe(studyCounts.enabled > 0);
      expect(this.controller.haveEnabledCentres).toBe(centreCounts.enabled > 0);
      expect(this.controller.updateEnabledStudies).toBeFunction();
      expect(this.controller.getEnabledStudiesPanelHeader).toBeFunction();
    });

  });

});
