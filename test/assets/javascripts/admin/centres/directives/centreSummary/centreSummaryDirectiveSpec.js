/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: centreSummaryDirective', function() {

    var createController = function (centre) {
      this.element = angular.element('<centre-summary centre="vm.centre"></centre-summary>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { centre: this.centre };

      this.eventRxFunc = jasmine.createSpy().and.returnValue(null);
      this.scope.$on('centre-view', this.eventRxFunc);

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('centreSummary');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(modalService, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Centre',
                              'CentreStatus',
                              'notificationsService',
                              'factory');

      self.centre = new self.Centre(self.factory.centre());

      spyOn(modalService, 'showModal').and.callFake(function () {
        return self.$q.when('modalResult');
      });

      self.putHtmlTemplates(
        '/assets/javascripts/admin/centres/directives/centreView/centreView.html',
        '/assets/javascripts/admin/centres/directives/centreSummary/centreSummary.html',
        '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
        '/assets/javascripts/common/directives/statusLine/statusLine.html');
    }));

    it('initialization is valid', function() {
      createController.call(this, this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
      expect(this.controller.descriptionToggleLength).toBeDefined();
      expect(this.eventRxFunc).toHaveBeenCalled();
    });

    describe('change centre status', function() {

      function checkStatusChange(centre, status, newStatus) {
        /* jshint validthis: true */
        var self = this;

        createController.call(self, self.centre);

        spyOn(self.Centre.prototype, status).and.callFake(function () {
          centre.status = (centre.status === self.CentreStatus.ENABLED) ?
            self.CentreStatus.DISABLED : self.CentreStatus.ENABLED;
          return self.$q.when(centre);
        });

        self.controller.changeStatus(status);
        self.scope.$digest();
        expect(self.Centre.prototype[status]).toHaveBeenCalled();
        expect(self.scope.vm.centre.status).toBe(newStatus);
      }

      it('should enable a centre', function() {
        checkStatusChange.call(this, this.centre, 'enable', this.CentreStatus.ENABLED);
      });

      it('should disable a centre', function() {
        this.centre.status = this.CentreStatus.ENABLED;
        checkStatusChange.call(this, this.centre, 'disable', this.CentreStatus.DISABLED);
      });

    });

  });

});
