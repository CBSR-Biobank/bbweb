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

  describe('Directive: ceventTypesPanelDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testUtils) {
      var self = this;

      self.Study        = self.$injector.get('Study');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.createEntities   = setupEntities(this);
      self.createController = setupController(this);

      testUtils.addCustomMatchers();

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/studies/ceventTypes/directives/ceventTypesPanel/ceventTypesPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');

      function setupEntities() {
        var CollectionEventType = self.$injector.get('CollectionEventType');

        return create;

        //--

        function create(options) {
          var entities = {};

          entities.study = new self.Study(self.jsonEntities.study());

          entities.ceventTypes = _.map(_.range(2), function () {
            var jsonCet = self.jsonEntities.collectionEventType(entities.study);
            return new CollectionEventType(jsonCet);
          });

          return entities;
        }
      }

      function setupController() {
        var $rootScope = self.$injector.get('$rootScope'),
            $compile   = self.$injector.get('$compile');

        return create;

        //--
        function create(entities) {
          self.element = angular.element([
            '<uib-accordion close-others="false">',
            '  <cevent-types-panel',
            '     study="vm.study"',
            '     cevent-types="vm.ceventTypes"',
            '     annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
            '  </cevent-types-panel>',
            '</uib-accordion>'
          ].join(''));

          self.scope = $rootScope.$new();
          self.scope.vm = {
            study:                  entities.study,
            ceventTypes:            entities.ceventTypes,
            annotationTypeIdsInUse: []
          };

          $compile(self.element)(self.scope);
          self.scope.$digest();
          self.controller = self.element.find('cevent-types-panel').controller('ceventTypesPanel');
        }
      }
    }));

    it('has valid scope', function () {
      var entities = this.createEntities();

      this.createController(entities);

      expect(this.controller.study).toBe(entities.study);
      expect(this.controller.ceventTypes).toBeArrayOfSize(entities.ceventTypes.length);
      expect(this.controller.ceventTypes).toContainAll(entities.ceventTypes);
    });

    it('changes to correct state to add a collection event type', function() {
      var state = this.$injector.get('$state'),
          entities = this.createEntities();

      this.createController(entities);

      spyOn(state, 'go').and.callFake(function () {});

      this.controller.add();
      this.scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('can view information for a collection event type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = this.createEntities();

      this.createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      this.controller.information(this.controller.ceventTypes[0]);
      this.scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for a specimen spec', function() {
      // var EntityViewer = this.$injector.get('EntityViewer'),
      //     entities = this.createEntities();

      // this.createController(entities);

      // spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      // this.controller.viewSpecimenGroup(entities.specimenGroups[0].id);
      // this.scope.$digest();
      // expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for an annotation type', function() {
      // var EntityViewer = this.$injector.get('EntityViewer'),
      //     entities = createEntities();

      // createController(entities);

      // spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      // controller.viewAnnotationType(entities.annotationTypes[0].id);
      // scope.$digest();
      // expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a collection event type if study is not disabled', function() {
      // var StudyStatus = this.$injector.get('StudyStatus'),
      //     statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()],
      //     entities = createEntities();

      // createController(entities);

      // _.each(statuses, function (status) {
      //   entities.study.status = status;

      //   expect(function () { controller.update(entities.ceventTypes[0]); }).
      //     toThrow(new Error('study is not disabled'));
      // });
    });

    it('can update a collection event type', function() {
      // var state = this.$injector.get('$state'),
      //     entities = createEntities();

      // createController(entities);

      // spyOn(state, 'go').and.callFake(function () {});

      // controller.update(entities.ceventTypes[0]);
      // scope.$digest();

      // expect(state.go).toHaveBeenCalledWith(
      //   'home.admin.studies.study.collection.ceventTypeUpdate',
      //   { ceventTypeId: entities.ceventTypes[0].id });
    });

    it('cannot remove a collection event type if study is not disabled', function() {
      // var StudyStatus = this.$injector.get('StudyStatus'),
      //     statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()],
      //     entities = createEntities();

      // createController(entities);

      // _.each(statuses, function (status) {
      //   entities.study.status = status;

      //   expect(function () { controller.remove(entities.ceventTypes[0]); }).
      //     toThrow(new Error('study is not disabled'));
      // });
    });

    it('can remove a collection event type', function() {
      // var q                   = this.$injector.get('$q'),
      //     domainEntityService = this.$injector.get('domainEntityService'),
      //     entities            = createEntities(),
      //     cetToRemove         = entities.ceventTypes[1];

      // createController(entities);

      // spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
      //   return q.when('OK');
      // });
      // controller.remove(cetToRemove);
      // scope.$digest();
      // expect(domainEntityService.removeEntity).toHaveBeenCalled();
      // expect(controller.ceventTypes).toBeArrayOfSize(entities.ceventTypes.length - 1);
    });

    it('displays a modal if removal of a collection event type fails', function() {
      // var q                   = this.$injector.get('$q'),
      //     modalService        = this.$injector.get('modalService'),
      //     entities            = createEntities(),
      //     cetToRemove         = entities.ceventTypes[1];

      // createController(entities);
      // spyOn(cetToRemove, 'remove').and.callFake(function () {
      //   var deferred = q.defer();
      //   deferred.reject('error');
      //   return deferred.promise;
      // });
      // spyOn(modalService, 'showModal').and.callFake(function () {
      //   return q.when('OK');
      // });

      // controller.remove(cetToRemove);
      // scope.$digest();
      // expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
