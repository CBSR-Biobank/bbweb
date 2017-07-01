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

  describe('Directive: studyAddDirective', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<study-add study="vm.study"></study-add>',
          { study: this.study },
          'studyAdd');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'Study',
                              'factory');

      this.study = new this.Study();
      this.titleContains = 'Add';
      this.returnState = 'home.admin.studies';

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studyAdd/studyAdd.html');
    }));

    it('should contain valid settings to update a study', function() {
      this.createController();
      expect(this.controller.study).toEqual(this.study);
    });

    it('should return to valid state on cancel', function() {
      var $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );
      this.createController();
      this.controller.cancel();
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState);
    });

    it('should return to valid state on invalid submit', function() {
      var $q                  = this.$injector.get('$q'),
          domainNotificationService = this.$injector.get('domainNotificationService');

      spyOn(domainNotificationService, 'updateErrorModal').and.callFake(function () {});
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('err');
        return deferred.promise;
      });

      this.createController();
      this.controller.submit(this.study);
      this.scope.$digest();
      expect(domainNotificationService.updateErrorModal)
        .toHaveBeenCalledWith('err', 'study');
    });

    it('should return to valid state on submit', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state');

      spyOn($state, 'go').and.callFake(function () {} );
      spyOn(this.Study.prototype, 'add').and.callFake(function () {
        return $q.when('test');
      });

      this.createController();
      this.controller.submit(this.study);
      this.scope.$digest();
      expect($state.go).toHaveBeenCalledWith(this.returnState, {}, { reload: true });
    });

  });

});
