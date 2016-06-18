/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: SpecimenGroupEditCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    describe('for collection event types', function() {
      var context = {};

      beforeEach(inject(function () {
        context.currentState         = 'home.admin.studies.study.collection.ceventType';
        context.returnState          = context.currentState;
      }));

      sharedBehaviour(context);
    });

    // TODO: enable this test once processing types GUI is refactored
    xdescribe('for processing types', function() {
      var context = {};

      beforeEach(inject(function () {
        context.currentState         = 'home.admin.studies.study.processing';
        context.returnState          = context.currentState;
      }));

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function() {

        beforeEach(inject(function(testUtils) {
          this.createEntities   = setupEntities(this);
          this.createController = setupController(this);
          this.context          = context;

          this.AnatomicalSourceType        = this.$injector.get('AnatomicalSourceType');
          this.PreservationType            = this.$injector.get('PreservationType');
          this.PreservationTemperatureType = this.$injector.get('PreservationTemperatureType');
          this.SpecimenType                = this.$injector.get('SpecimenType');

          testUtils.addCustomMatchers();
        }));

        function setupEntities(userContext) {
          var Study         = userContext.$injector.get('Study'),
              SpecimenGroup = userContext.$injector.get('SpecimenGroup'),
              factory  = userContext.$injector.get('factory');

          return create;

          //--

          function create(options) {
            var specimenGroup,
                study = new Study(factory.study());

            options = options || {};
            if (options.noSgId) {
              specimenGroup = new SpecimenGroup(_.omit(factory.processingType(study), 'id'));
            } else {
              specimenGroup = new SpecimenGroup(factory.processingType(study));
            }

            return {
              study:         study,
              specimenGroup: specimenGroup
            };
          }
        }

        function setupController(userContext) {
          var $rootScope           = userContext.$injector.get('$rootScope'),
              $controller          = userContext.$injector.get('$controller'),
              $state               = userContext.$injector.get('$state'),
              domainEntityService  = userContext.$injector.get('domainEntityService'),
              notificationsService = userContext.$injector.get('notificationsService');

          return create;

          //--

          function create(entities) {
            userContext.scope = $rootScope.$new();

            $state.current.name = userContext.context.currentState;

            $controller('SpecimenGroupEditCtrl as vm', {
              $scope:                      userContext.scope,
              $state:                      $state,
              domainEntityService:         domainEntityService,
              notificationsService:        notificationsService,
              AnatomicalSourceType:        userContext.AnatomicalSourceType,
              PreservationType:            userContext.PreservationType,
              PreservationTemperatureType: userContext.PreservationTemperatureType,
              SpecimenType:                userContext.SpecimenType,
              study:                       entities.study,
              specimenGroup:               entities.specimenGroup
            });

            userContext.scope.$digest();
          }
        }

        describe('has valid scope when created', function () {

          it('for new specimen group', function() {
            var entities = this.createEntities({ noSgId: true });

            this.createController(entities);

            expect(this.scope.vm.title).toBe('Add Specimen Group');
            checkInitialScope(this, entities);
          });

          it('for existing specimen group', function() {
            var entities = this.createEntities();
            this.createController(entities);
            expect(this.scope.vm.title).toBe('Update Specimen Group');
            checkInitialScope(this, entities);
          });

          function checkInitialScope(userContext, entities) {
            expect(userContext.scope.vm.study).toBe(entities.study);
            expect(userContext.scope.vm.specimenGroup).toBe(entities.specimenGroup);
            expect(userContext.scope.vm.anatomicalSourceTypes ).toBeDefined();
            expect(userContext.scope.vm.preservTypes).toBeDefined();
            expect(userContext.scope.vm.preservTempTypes).toBeDefined();
            expect(userContext.scope.vm.specimenTypes).toBeDefined();
          }

        });

        it('can submit a specimen group', function() {
          var $q       = this.$injector.get('$q'),
              $state   = this.$injector.get('$state'),
              entities = this.createEntities();

          this.createController(entities);
          spyOn(entities.specimenGroup, 'addOrUpdate').and.callFake(function () {
            return $q.when(entities.specimenGroup);
          });
          spyOn($state, 'go').and.callFake(function () {});
          this.scope.vm.submit(entities.specimenGroup);
          this.scope.$digest();

          expect($state.go).toHaveBeenCalledWith(
            this.context.currentState, {}, { reload: true });
        });

        it('on submit error, displays an error modal', function() {
          var q                   = this.$injector.get('$q'),
              domainEntityService = this.$injector.get('domainEntityService'),
              entities            = this.createEntities();

          this.createController(entities);
          spyOn(entities.specimenGroup, 'addOrUpdate').and.callFake(function () {
            var deferred = q.defer();
            deferred.reject('xxx');
            return deferred.promise;
          });
          spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});

          this.scope.vm.submit(entities.specimenGroup);
          this.scope.$digest();

          expect(domainEntityService.updateErrorModal)
            .toHaveBeenCalledWith('xxx', 'specimen group');
        });

        it('when user presses the cancel button, goes to correct state', function() {
          var state    = this.$injector.get('$state'),
              entities            = this.createEntities();

          this.createController(entities);
          spyOn(state, 'go').and.callFake(function () {});
          this.scope.vm.cancel();
          this.scope.$digest();
          expect(state.go).toHaveBeenCalledWith(
            this.context.currentState, {}, { reload: true });
        });

      });

    }

  });

});
