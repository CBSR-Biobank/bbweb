/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudyEditCtrl', function() {

    var Study, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_Study_, fakeDomainEntities) {
      Study = _Study_;
      fakeEntities = fakeDomainEntities;
    }));

    describe('when adding a study', function() {
      var context = {};

      beforeEach(function() {
        context.study = new Study();
        context.titleContains = 'Add';
        context.returnState = {
          name: 'home.admin.studies',
          params: {}
        };
      });

      sharedBehaviour(context);
    });

    describe('when updating a study', function() {

      var context = {};

      beforeEach(function() {
        context.study = new Study(fakeEntities.study());
        context.titleContains = 'Update';
        context.returnState = {
          name: 'home.admin.studies.study.summary',
          params: {studyId: context.study.id}
        };
      });

      sharedBehaviour(context);

    });

    function sharedBehaviour(context) {

      describe('(shared)', function () {

        var study, titleContains, returnState, createController;

        function setupController(injector) {
          var $rootScope           = injector.get('$rootScope'),
              $controller          = injector.get('$controller'),
              $state               = injector.get('$state'),
              notificationsService = injector.get('notificationsService'),
              domainEntityService  = injector.get('domainEntityService');

          return create;

          //--

          function create(study) {
            var scope = $rootScope.$new();
            $controller('StudyEditCtrl as vm', {
              $scope:               scope,
              $state:               $state,
              notificationsService: notificationsService,
              domainEntityService:  domainEntityService,
              study:                study
            });
            scope.$digest();
            return scope;
          }
        }

        beforeEach(inject(function ($injector) {
          study = context.study;
          titleContains = context.titleContains;
          returnState = context.returnState;
          createController = setupController($injector);
        }));

        it('should contain valid settings to update a study', function() {
          var scope = createController(study);

          expect(scope.vm.study).toEqual(study);
          expect(scope.vm.title).toContain(titleContains);
          expect(scope.vm.returnState.name).toBe(returnState.name);
          expect(scope.vm.returnState.params).toEqual(returnState.params);
        });

        it('should return to valid state on cancel', function() {
          var $state = this.$injector.get('$state'),
              scope = createController(study);
          spyOn($state, 'go').and.callFake(function () {} );
          scope.vm.cancel();
          expect($state.go).toHaveBeenCalledWith(returnState.name,
                                                 returnState.params,
                                                 { reload: false });
        });

        it('should return to valid state on invalid submit', function() {
          var $q                  = this.$injector.get('$q'),
              domainEntityService = this.$injector.get('domainEntityService'),
              scope               = createController(study);

          spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
          spyOn(Study.prototype, 'addOrUpdate').and.callFake(function () {
            var deferred = $q.defer();
            deferred.reject('err');
            return deferred.promise;
          });

          scope.vm.submit(study);
          scope.$digest();
          expect(domainEntityService.updateErrorModal)
            .toHaveBeenCalledWith('err', 'study');
        });


        it('should return to valid state on submit', function() {
          var $q     = this.$injector.get('$q'),
              $state = this.$injector.get('$state'),
              scope  = createController(study);

          spyOn($state, 'go').and.callFake(function () {} );
          spyOn(Study.prototype, 'addOrUpdate').and.callFake(function () {
            return $q.when('test');
          });

          scope.vm.submit(study);
          scope.$digest();
          expect($state.go).toHaveBeenCalledWith(returnState.name,
                                                 returnState.params,
                                                 { reload: true });
        });

      });

    }

  });

});
