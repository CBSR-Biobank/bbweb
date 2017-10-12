/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

xdescribe('ProcessingType', function() {

  var httpBackend,
      ProcessingType,
      factory;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function($httpBackend,
                                 _ProcessingType_,
                                 _factory_,
                                 ServerReplyMixin) {
      _.extend(this, ServerReplyMixin.prototype);
    httpBackend    = $httpBackend;
      ProcessingType = _ProcessingType_;
      factory        = _factory_;
    });
  });

  function uri(/* studyId, processingTypeId, version */) {
    var args = _.toArray(arguments), studyId, processingTypeId, version, result;

    if (arguments.length < 1) {
      throw new Error('study id not specified');
    }

    studyId = args.shift();
    result = '/studies/' + studyId + '/proctypes';

    if (args.length > 0) {
      processingTypeId = args.shift();
      result += '/' + processingTypeId;
    }
    if (args.length > 0) {
      version = args.shift();
      result += '/' + version;
    }
    return result;
  }

  function createEntities(options) {
    var study = factory.study(),
        serverPt = factory.processingType(study),
        processingType;

    options = options || {};

    if (options.noPtId) {
      processingType = new ProcessingType(_.omit(serverPt, 'id'));
    } else {
      processingType = new ProcessingType(serverPt);
    }
    return {
      study:          study,
      serverPt:       serverPt,
      processingType: processingType
    };
  }

  it('constructor with no parameters has default values', function() {
    var processingType = new ProcessingType();

    expect(processingType.isNew()).toBe(true);
    expect(processingType.studyId).toBe(null);
    expect(processingType.name).toBe('');
    expect(processingType.description).toBe(null);
    expect(processingType.enabled).toBe(false);
  });

  it('fails when creating from a non object', function() {
    expect(ProcessingType.create(1))
      .toEqual(new Error('invalid object from server: must be a map, has the correct keys'));
  });

  it('can retrieve a processing type', function() {
    fail('needs implementation');
  });

  it('can list processing types', function() {
    fail('needs implementation');
  });

  it('can add a processing type', function() {
    fail('needs implementation');
  });

  it('can update a processing type', function() {
    fail('needs implementation');
  });

  it('should remove a processing type', function() {
    var entities = createEntities();

    httpBackend.expectDELETE(uri(entities.study.id, entities.processingType.id, entities.processingType.version))
      .respond(this.reply(true));

    entities.processingType.remove();
    httpBackend.flush();
  });

  it('isNew should be true for a processing type with no ID', function() {
    var entities = createEntities({ noPtId: true });
    expect(entities.processingType.isNew()).toBe(true);
  });

  it('isNew should be false for a processing type that has an ID', function() {
    var entities = createEntities();
    expect(entities.processingType.isNew()).toBe(false);
  });

  it('study ID matches the study', function() {
    var entities = createEntities();
    expect(entities.processingType.studyId).toBe(entities.study.id);
  });

  function processingTypeToAddCommand(processingType) {
    return {
      studyId:     processingType.studyId,
      name:        processingType.name,
      description: processingType.description,
      enabled:     processingType.enabled
    };
  }

  function processingTypeToUpdateCommand(processingType) {
    return _.extend(processingTypeToAddCommand(processingType), {
      id: processingType.id,
      expectedVersion: processingType.version
    });
  }
});
