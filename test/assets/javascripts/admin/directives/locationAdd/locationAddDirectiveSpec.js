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

    var createController = function (onSubmit, onCancel) {
      this.element = angular.element([
        '<location-add ',
        '  on-submit="vm.onSubmit"',
        '  on-cancel="vm.onCancel">',
        '</location-add>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        onSubmit: onSubmit,
        onCancel: onCancel
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('locationAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($state, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Location',
                              'factory',
                              'domainEntityService',
                              'notificationsService');

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

    }));

    it('scope should be valid', function() {
      createController.call(this, this.onSubmit, this.onCancel);
      expect(this.controller.onSubmit).toBeFunction();
      expect(this.controller.onCancel).toBeFunction();
    });

    it('should invoke function on submit', function() {
      var location = new this.Location();

      createController.call(this, this.onSubmit, this.onCancel);
      this.controller.submit(location);
      this.scope.$digest();
      expect(this.onSubmit).toHaveBeenCalledWith(location);
    });

    it('should invoke function on cancel', function() {
      createController.call(this, this.onSubmit, this.onCancel);
      this.controller.cancel();
      this.scope.$digest();
      expect(this.onCancel).toHaveBeenCalled();
    });

  });

});
