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

    beforeEach(inject(function (testUtils) {
      this.$q                = this.$injector.get('$q');
      this.Study             = this.$injector.get('Study');
      this.jsonEntities      = this.$injector.get('jsonEntities');
      this.createStudyCounts = setupCountsCreator(this);
      this.createController  = setupController(this);
      testUtils.addCustomMatchers();

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/studiesList/studiesList.html',
        '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');
    }));

    function setupCountsCreator(userContext) {
      var StudyCounts = userContext.$injector.get('StudyCounts');

      return create;

      //--

      function create(disabled, enabled, retired) {
        return new StudyCounts({
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        });
      }
    }

    function setupController(userContext) {
      var $rootScope = userContext.$injector.get('$rootScope'),
          $compile   = userContext.$injector.get('$compile');

      return create;

      //---

      function create(studyCounts) {
        var element = angular.element([
          '<studies-list',
          '  study-counts="vm.studyCounts">',
          '</studies-list>'
        ].join(''));

        userContext.scope = $rootScope.$new();
        userContext.scope.vm = { studyCounts: studyCounts };
        $compile(element)(userContext.scope);
        userContext.scope.$digest();
        userContext.controller = element.controller('studiesList');
      }
    }

    it('scope is valid on startup', function() {
      var self        = this,
          StudyStatus = this.$injector.get('StudyStatus'),
          allStatuses = StudyStatus.values(),
          counts      = this.createStudyCounts(1, 2, 3);

      spyOn(self.Study, 'list').and.callFake(function () {
        return self.$q.when(self.jsonEntities.pagedResult([]));
      });

      self.createController(counts);

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
