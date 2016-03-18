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

  xdescribe('Directive: processingTypesPanelDirective', function() {
    var scope,
        controller,
        createEntities,
        createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (directiveTestSuite, testUtils) {
      _.extend(this, directiveTestSuite);

      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/processing/processingTypesPanel/processingTypesPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');
    }));

    function setupEntities(injector) {
      var Study          = injector.get('Study'),
          ProcessingType = injector.get('ProcessingType'),
          jsonEntities   = injector.get('jsonEntities');

      return create;

      //--

      function create() {
        var entities = {};
        entities.study = new Study(jsonEntities.study());
        entities.processingTypes = _.map(_.range(2), function () {
          return new ProcessingType(jsonEntities.processingType(entities.study));
        });
        return entities;
      }
    }

    function setupController(injector) {
      var rootScope = injector.get('$rootScope'),
          compile   = injector.get('$compile');

      return create;

      //--

      function create(study, processingTypes) {
        var element;

        element = angular.element([
          '<uib-accordion close-others="false">',
          '  <processing-types-panel',
          '     study="vm.study"',
          '     processing-types="vm.processingTypes">',
          '  </processing-types-panel>',
          '</uib-accordion>'
        ].join(''));

        scope = rootScope.$new();

        scope.vm = {
          study: study,
          processingTypes: processingTypes
        };

        compile(element)(scope);
        scope.$digest();
        controller = element.find('processing-types-panel')
          .controller('processingTypesPanel');
      }
    }

    it('has valid scope', function () {
      var entities = createEntities();

      createController(entities.study, entities.processingTypes);
      expect(controller.study).toBe(entities.study);
      expect(controller.processingTypes).toBeArrayOfSize(entities.processingTypes.length);
      expect(controller.processingTypes).toContainAll(entities.processingTypes);
    });

    it('can add a processing type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities();

      createController(entities.study, entities.processingTypes);
      spyOn(state, 'go').and.callFake(function () {});
      controller.add();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.processingTypeAdd');
    });

    it('can view information for a processing type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities();

      createController(entities.study, entities.processingTypes);
      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      controller.information(entities.processingTypes[0]);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a processing type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities = createEntities(),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        entities.study.status = status;
        createController(entities.study, entities.processingTypes);
        expect(function () { controller.update(entities.processingTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can update a processing type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          processingType = entities.processingTypes[0];

      createController(entities.study, entities.processingTypes);
      spyOn(state, 'go').and.callFake(function () {});
      controller.update(processingType);
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.processingTypeUpdate',
        { processingTypeId: processingType.id });
    });

    it('cannot update a processing type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities = createEntities(),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        entities.study.status = status;
        createController(entities.study, entities.processingTypes);
        expect(function () { controller.remove(entities.processingTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can remove a processing type', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          ptToRemove          = entities.processingTypes[1];

      createController(entities.study, entities.processingTypes);
      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      controller.remove(ptToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(controller.processingTypes).toBeArrayOfSize(entities.processingTypes.length - 1);
    });

    it('displays a modal if removal of a processing type fails', function() {
      var q             = this.$injector.get('$q'),
          modalService  = this.$injector.get('modalService'),
          entities      = createEntities(),
          ptToRemove    = entities.processingTypes[1];

      createController(entities.study, entities.processingTypes);
      spyOn(ptToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });

      controller.remove(ptToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });


  });

});
