/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreEditCtrl', function() {

    var Centre, jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_Centre_, jsonEntities) {
      Centre = _Centre_;
      jsonEntities = jsonEntities;
    }));

    describe('when adding a centre', function() {
      var context = {};

      beforeEach(function() {
        context.centre = new Centre();
        context.titleContains = 'Add';
        context.returnState = {
          name: 'home.admin.centres',
          params: {}
        };
      });

      sharedBehaviour(context);
    });

    describe('when updating a centre', function() {

      var context = {};

      beforeEach(function() {
        context.centre = new Centre(jsonEntities.centre());
        context.titleContains = 'Update';
        context.returnState = {
          name: 'home.admin.centres.centre.summary',
          params: {centreId: context.centre.id}
        };
      });

      sharedBehaviour(context);

    });

    function sharedBehaviour(context) {

      describe('(shared)', function () {

        var centre, titleContains, returnState, createController;

        function setupController(injector) {
          var $rootScope           = injector.get('$rootScope'),
              $controller          = injector.get('$controller'),
              $state               = injector.get('$state'),
              notificationsService = injector.get('notificationsService'),
              domainEntityService  = injector.get('domainEntityService');

          return create;

          //--

          function create(centre) {
            var scope = $rootScope.$new();
            $controller('CentreEditCtrl as vm', {
              $scope:               scope,
              $state:               $state,
              notificationsService: notificationsService,
              domainEntityService:  domainEntityService,
              centre:                centre
            });
            scope.$digest();
            return scope;
          }
        }

        beforeEach(inject(function ($injector) {
          centre = context.centre;
          titleContains = context.titleContains;
          returnState = context.returnState;
          createController = setupController($injector);
        }));

        it('scope should be valid', function() {
          var scope = createController(centre);

          expect(scope.vm.centre).toEqual(centre);
          expect(scope.vm.title).toContain(titleContains);
          expect(scope.vm.returnState.name).toBe(returnState.name);
          expect(scope.vm.returnState.params).toEqual(returnState.params);
        });

        it('should return to valid state on cancel', function() {
          var $state = this.$injector.get('$state'),
              scope = createController(centre);
          spyOn($state, 'go').and.callFake(function () {} );
          scope.vm.cancel();
          expect($state.go).toHaveBeenCalledWith(returnState.name,
                                                 returnState.params,
                                                 { reload: false });
        });

        it('should return to valid state on invalid submit', function() {
          var $q                  = this.$injector.get('$q'),
              domainEntityService = this.$injector.get('domainEntityService'),
              scope               = createController(centre);

          spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
          spyOn(Centre.prototype, 'addOrUpdate').and.callFake(function () {
            var deferred = $q.defer();
            deferred.reject('err');
            return deferred.promise;
          });

          scope.vm.submit(centre);
          scope.$digest();
          expect(domainEntityService.updateErrorModal)
            .toHaveBeenCalledWith('err', 'centre');
        });


        it('should return to valid state on submit', function() {
          var $q     = this.$injector.get('$q'),
              $state = this.$injector.get('$state'),
              scope  = createController(centre);

          spyOn($state, 'go').and.callFake(function () {} );
          spyOn(Centre.prototype, 'addOrUpdate').and.callFake(function () {
            return $q.when('test');
          });

          scope.vm.submit(centre);
          scope.$digest();
          expect($state.go).toHaveBeenCalledWith(returnState.name,
                                                 returnState.params,
                                                 { reload: true });
        });

      });

    }

  });

});
