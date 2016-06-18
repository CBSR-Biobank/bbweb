/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'lodash',
  'angularMocks',
  'biobankApp'
], function(angular, _, mocks) {
  'use strict';

  describe('Directive: centresListDirective', function() {

    var createCentreCounts = function (disabled, enabled, retired) {
      return new this.CentreCounts({
        total:    disabled + enabled + retired,
        disabled: disabled,
        enabled:  enabled,
        retired:  retired
      });
    };

    var createController = function (centreCounts) {
      this.element = angular.element('<centres-list></centres-list>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centresList');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'CentreCounts',
                              'Centre',
                              'CentreStatus',
                              'factory');

      spyOn(self.Centre, 'list').and.callFake(function () {
        return self.$q.when(self.factory.pagedResult([]));
      });

      testUtils.addCustomMatchers();

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centresList/centresList.html',
        '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');
    }));

    it('scope is valid on startup', function() {
      var self = this,
          CentreStatus = self.$injector.get('CentreStatus'),
          centreStatusLabel = self.$injector.get('centreStatusLabel'),
          allStatuses = _.values(CentreStatus),
          counts = createCentreCounts.call(self, 1, 2, 3);

      spyOn(self.CentreCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      createController.call(self, counts);

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
          counts = createCentreCounts.call(this, 1, 2, 3),
          listOptions = {};

      spyOn(self.CentreCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      createController.call(self, counts);
      self.controller.updateCentres(listOptions);
      self.scope.$digest();

      expect(Centre.list).toHaveBeenCalledWith(listOptions);
    });


  });

});
