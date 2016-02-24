/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: LocationEditCtrl', function() {
    var Centre, Location, jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_Centre_,
                                _Location_,
                                jsonEntities) {
      Centre       = _Centre_;
      Location     = _Location_;
      jsonEntities = jsonEntities;
    }));

    describe('when adding a location', function() {
      var context = {};

      beforeEach(function() {
        context.centre = new Centre(jsonEntities.centre());
        context.location = new Location();
        context.titleContains = 'Add';
        context.currentState = {
          current: { name: 'home.admin.centres.centre.locationAdd'}
        };
        context.returnState = {
          name: 'home.admin.centres.centre.locations',
          params: {}
        };
      });

      sharedBehaviour(context);
    });

    describe('when updating a location', function() {

      var context = {};

      beforeEach(function() {
        context.centre = new Centre(jsonEntities.centre());
        context.location = new Location(jsonEntities.location());

        context.centre.locations.push(context.location);

        context.titleContains = 'Update';
        context.currentState = {
          current: { name: 'home.admin.centres.centre.locationUpdate'},
          params: { locationId: context.location.id }
        };
        context.returnState = {
          name: 'home.admin.centres.centre.locations',
          params: {}
        };
      });

      sharedBehaviour(context);

    });

    function sharedBehaviour(context) {

      describe('(shared)', function () {

        var centre, location, state, titleContains, currentState, returnState, createController;

        function setupController(injector) {
          var $rootScope           = injector.get('$rootScope'),
              $controller          = injector.get('$controller'),
              Location             = injector.get('Location'),
              domainEntityService  = injector.get('domainEntityService'),
              notificationsService = injector.get('notificationsService');

          return create;

          //--

          function create(location) {
            var scope = $rootScope.$new();

            $controller('LocationEditCtrl as vm', {
              $scope:               scope,
              $state:               state,
              Location:             Location,
              domainEntityService:  domainEntityService,
              notificationsService: notificationsService,
              centre:               centre
            });
            scope.$digest();
            return scope;
          }
        }

        beforeEach(inject(function ($injector) {
          centre = context.centre;
          location = context.location;
          titleContains = context.titleContains;
          currentState = context.currentState;
          returnState = context.returnState;
          createController = setupController($injector);

          state = currentState;
          state.go = function () {};
        }));

        it('scope should be valid', function() {
          var scope = createController(location);

          expect(scope.vm.centre).toBe(centre);
          expect(scope.vm.location).toEqual(location);
          expect(scope.vm.title).toContain(titleContains);
        });

        it('should return to valid state on cancel', function() {
          var scope = createController(location);

          spyOn(state, 'go').and.callFake(function () {} );
          scope.vm.cancel();
          expect(state.go).toHaveBeenCalledWith(returnState.name,
                                                 returnState.params,
                                                 { reload: false });
        });

        it('should display failure information on invalid submit', function() {
          var $q                  = this.$injector.get('$q'),
              domainEntityService = this.$injector.get('domainEntityService'),
              scope               = createController(location);

          spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});
          spyOn(Centre.prototype, 'addLocation').and.callFake(function () {
            var deferred = $q.defer();
            deferred.reject('err');
            return deferred.promise;
          });

          scope.vm.submit(location);
          scope.$digest();
          expect(domainEntityService.updateErrorModal)
            .toHaveBeenCalledWith('err', 'location');
        });


        it('should return to valid state on submit', function() {
          var $q     = this.$injector.get('$q'),
              scope  = createController(location);

          spyOn(state, 'go').and.callFake(function () {} );
          spyOn(Centre.prototype, 'addLocation').and.callFake(function () {
            return $q.when('test');
          });

          scope.vm.submit(location);
          scope.$digest();
          expect(state.go).toHaveBeenCalledWith(returnState.name,
                                                returnState.params,
                                                { reload: true });
        });

      });

    }

  });

});
