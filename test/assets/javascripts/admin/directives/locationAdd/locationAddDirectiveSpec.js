/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: locationAddDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $state, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$state               = self.$injector.get('$state');
      self.Centre               = self.$injector.get('Centre');
      self.Location             = self.$injector.get('Location');
      self.factory         = self.$injector.get('factory');
      self.domainEntityService  = self.$injector.get('domainEntityService');
      self.notificationsService = self.$injector.get('notificationsService');

      self.centre = new self.Centre(self.factory.centre());
      self.location = new self.Location();

      self.currentState = {
        current: { name: 'home.admin.centres.centre.locationAdd'}
      };

      self.onSubmit = jasmine.createSpy('onSubmit');
      self.onCancel = jasmine.createSpy('onCancel');
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/locationAdd/locationAdd.html');

      //--

      function createController(onSubmit, onCancel) {
        self.element = angular.element([
          '<location-add ',
          '  on-submit="vm.onSubmit"',
          '  on-cancel="vm.onCancel">',
          '</location-add>'
        ].join(''));
        self.scope = $rootScope.$new();
        self.scope.vm = {
          onSubmit: onSubmit,
          onCancel: onCancel
        };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('locationAdd');
      }

    }));

    it('scope should be valid', function() {
      this.createController(this.onSubmit, this.onCancel);
      expect(this.controller.onSubmit).toBeFunction();
      expect(this.controller.onCancel).toBeFunction();
    });

    it('should invoke function on submit', function() {
      var location = new this.Location();

      this.createController(this.onSubmit, this.onCancel);
      this.controller.submit(location);
      this.scope.$digest();
      expect(this.onSubmit).toHaveBeenCalledWith(location);
    });

    it('should invoke function on cancel', function() {
      this.createController(this.onSubmit, this.onCancel);
      this.controller.cancel();
      this.scope.$digest();
      expect(this.onCancel).toHaveBeenCalled();
    });

  });

});
