/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Directive: centreAddDirective()', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'Centre',
                              'Factory',
                              'notificationsService',
                              'domainNotificationService');

      this.centre = new this.Centre();
      this.returnState = {
        name: 'home.admin.centres',
        params: {}
      };

      this.createController = (centre) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<centre-add centre="vm.centre"></centre-add>',
          { centre: centre },
          'centreAdd');
    });
  });

  it('scope should be valid', function() {
    this.createController(this.centre);
    expect(this.scope.vm.centre).toEqual(this.centre);
    expect(this.controller.returnState.name).toBe(this.returnState.name);
    expect(this.controller.returnState.params).toEqual(this.returnState.params);
  });

  it('should return to valid state on cancel', function() {
    var $state = this.$injector.get('$state');

    this.createController(this.centre);
    spyOn($state, 'go').and.callFake(function () {} );
    this.controller.cancel();
    expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                           this.returnState.params,
                                           { reload: false });
  });

  it('should return to valid state on invalid submit', function() {
    var $q                  = this.$injector.get('$q'),
        domainNotificationService = this.$injector.get('domainNotificationService');

    this.createController(this.centre);
    spyOn(domainNotificationService, 'updateErrorModal').and.callFake(function () {});
    spyOn(this.Centre.prototype, 'add').and.callFake(function () {
      var deferred = $q.defer();
      deferred.reject('err');
      return deferred.promise;
    });

    this.controller.submit(this.centre);
    this.scope.$digest();
    expect(domainNotificationService.updateErrorModal).toHaveBeenCalledWith('err', 'centre');
  });


  it('should return to valid state on submit', function() {
    var $q     = this.$injector.get('$q'),
        $state = this.$injector.get('$state');

    this.createController(this.centre);

    spyOn($state, 'go').and.callFake(function () {} );
    spyOn(this.Centre.prototype, 'add').and.callFake(function () {
      return $q.when('test');
    });

    this.controller.submit(this.centre);
    this.scope.$digest();
    expect($state.go).toHaveBeenCalledWith(this.returnState.name,
                                           this.returnState.params,
                                           { reload: true });
  });

});
