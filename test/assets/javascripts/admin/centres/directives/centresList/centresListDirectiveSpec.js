/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'underscore',
  'angularMocks',
  'biobankApp'
], function(angular, _, mocks) {
  'use strict';

  describe('Directive: centresListDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q           = self.$injector.get('$q');
      self.CentreCounts = self.$injector.get('CentreCounts');
      self.Centre       = self.$injector.get('Centre');
      self.CentreStatus = self.$injector.get('CentreStatus');
      self.factory = self.$injector.get('factory');

      self.createController = createController;
      self.createCentreCounts = createCentreCounts;

      spyOn(self.Centre, 'list').and.callFake(function () {
        return self.$q.when(self.factory.pagedResult([]));
      });

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centresList/centresList.html',
        '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');

      function createCentreCounts(disabled, enabled, retired) {
        return new self.CentreCounts({
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        });
      }

      function createController(centreCounts) {
        self.element = angular.element('<centres-list></centres-list>');
        self.scope = $rootScope.$new();
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centresList');
      }
    }));

    it('scope is valid on startup', function() {
      var self = this,
          CentreStatus = self.$injector.get('CentreStatus'),
          centreStatusLabel = self.$injector.get('centreStatusLabel'),
          allStatuses = _.values(CentreStatus),
          counts = self.createCentreCounts(1, 2, 3);

      spyOn(self.CentreCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      self.createController(counts);

      expect(self.controller.centreCounts).toEqual(counts);
      expect(self.controller.pageSize).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(self.controller.possibleStatuses).toContain({
          id: status,
          label: centreStatusLabel.statusToLabel(status)
        });
      });
      expect(self.controller.possibleStatuses).toContain({ id: 'all', label: 'All'});
    });

    it('updateCentres retrieves new list of centres', function() {
      var self = this,
          Centre = this.$injector.get('Centre'),
          counts = this.createCentreCounts(1, 2, 3),
          listOptions = {};

      spyOn(self.CentreCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      self.createController(counts);
      self.controller.updateCentres(listOptions);
      self.scope.$digest();

      expect(Centre.list).toHaveBeenCalledWith(listOptions);
    });


  });

});
