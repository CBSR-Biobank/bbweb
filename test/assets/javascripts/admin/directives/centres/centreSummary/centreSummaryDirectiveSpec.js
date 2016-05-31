/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: centreSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, modalService, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                   = self.$injector.get('$q');
      self.Centre               = self.$injector.get('Centre');
      self.CentreStatus         = self.$injector.get('CentreStatus');
      self.notificationsService = self.$injector.get('notificationsService');
      self.factory              = self.$injector.get('factory');

      self.centre = new self.Centre(self.factory.centre());

      spyOn(modalService, 'showModal').and.callFake(function () {
        return self.$q.when('modalResult');
      });

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/centres/centreSummary/centreSummary.html',
        '/assets/javascripts/common/directives/truncateToggle.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html');

      self.createController = createController;

      //--

      function createController(centre) {
        self.element = angular.element('<centre-summary centre="vm.centre"></centre-summary>');
        self.scope = $rootScope.$new();
        self.scope.vm = { centre: self.centre };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('centreSummary');
      }
    }));

    it('should contain valid settings to display the centre summary', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
      expect(this.controller.descriptionToggleLength).toBeDefined();
    });

    describe('change centre status', function() {

      function checkStatusChange(centre, status, newStatus) {
        /* jshint validthis: true */
        var self = this;

        self.createController(self.centre);

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
