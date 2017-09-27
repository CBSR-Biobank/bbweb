/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'lodash', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  xdescribe('Controller: ProcessingTypeEditCtrl', function() {

    var q,
        rootScope,
        controller,
        state,
        Study,
        ProcessingType,
        domainNotificationService,
        notificationsService,
        factory;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               $state,
                               _Study_,
                               _ProcessingType_,
                               _domainNotificationService_,
                               _notificationsService_,
                               _factory_) {
      q                          = $q;
      rootScope                  = $rootScope;
      controller                 = $controller;
      state                      = $state;
      Study                      = _Study_;
      ProcessingType             = _ProcessingType_;
      domainNotificationService        = _domainNotificationService_;
      notificationsService       = _notificationsService_;
      factory               = _factory_;
    }));

    function createEntities(options) {
      var study, serverPt, processingType;

      options = options || {};

      study = new Study(factory.study());

      if (options.noPtId) {
        serverPt = _.omit(factory.processingType(study), 'id');
      } else {
        serverPt = factory.processingType(study);
      }

      processingType = new ProcessingType(serverPt);

      return {
        study:          study,
        serverPt:       serverPt,
        processingType: processingType
      };
    }

    function createController(entities) {
      var scope = rootScope.$new();

      controller('ProcessingTypeEditCtrl as vm', {
        $scope:               scope,
        $state:               state,
        domainNotificationService:  domainNotificationService,
        notificationsService: notificationsService,
        processingType:       entities.processingType
      });

      scope.$digest();
      return scope;
    }

    it('scope is valid when created from a new processing type', function() {
      var entities = createEntities({ noPtId: true }),
          scope = createController(entities);
      expect(scope.vm.title).toBe('Add Processing Type');
      expect(scope.vm.processingType).toBe(entities.processingType);
    });

    it('scope is valid when created from an existing processing type', function() {
      var entities = createEntities(),
          scope = createController(entities);
      expect(scope.vm.title).toBe('Update Processing Type');
      expect(scope.vm.processingType).toBe(entities.processingType);
    });

    it('on submit success, changes to correct state', function() {
      var entities = createEntities(),
          scope = createController(entities);
      spyOn(state, 'go').and.callFake(function () {});
      spyOn(entities.processingType, 'addOrUpdate').and.callFake(function () {
        return q.when(entities.processingType);
      });
      scope.vm.submit(scope.vm.processingType);
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing', {}, {reload: true});
    });

    it('on submit failure, changes to correct state', function() {
      var entities = createEntities(),
          scope = createController(entities);
      spyOn(domainNotificationService, 'updateErrorModal').and.callFake(function () {});
      spyOn(entities.processingType, 'addOrUpdate').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      scope.vm.submit(scope.vm.processingType);
      scope.$digest();
      expect(domainNotificationService.updateErrorModal).toHaveBeenCalledWith(
        'error', 'processing type');
    });

    it('on cancel changes to correct state', function() {
      var entities = createEntities(),
          scope = createController(entities);
      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.cancel();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing', {}, {reload: true});
    });


  });

});
