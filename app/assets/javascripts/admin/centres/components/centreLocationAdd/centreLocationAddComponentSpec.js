/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: centreLocationAdd', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (centre) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<centre-location-add centre="vm.centre"></centre-location-add>',
          { centre: centre },
          'centreLocationAdd');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Location',
                              'factory',
                              'domainNotificationService',
                              'notificationsService');

      this.centre = new this.Centre(this.factory.centre());
      this.location = new this.Location(this.factory.location());
      this.returnStateName = 'home.admin.centres.centre.locations';

      this.putHtmlTemplates(
        '/assets/javascripts/admin/centres/components/centreLocationAdd/centreLocationAdd.html',
        '/assets/javascripts/admin/components/locationAdd/locationAdd.html');
    }));

    it('scope should be valid', function() {
      this.createController(this.centre);
      expect(this.controller.centre).toBe(this.centre);
      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    it('should return to valid state on cancel', function() {
      this.createController(this.centre);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      this.controller.cancel();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName);
    });

    it('should display failure information on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainNotificationService = this.$injector.get('domainNotificationService');

      this.createController(this.centre);

      spyOn(domainNotificationService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.controller.submit(this.location);
      this.scope.$digest();
      expect(domainNotificationService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'location');
    });


    it('should return to valid state on submit', function() {
      var $q = this.$injector.get('$q');

      this.createController(this.centre);
      spyOn(this.$state, 'go').and.callFake(function () {} );
      spyOn(this.Centre.prototype, 'addLocation').and.callFake(function () {
        return $q.when('test');
      });

      this.controller.submit(this.location);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(this.returnStateName, {}, { reload: true });
    });

  });

});
