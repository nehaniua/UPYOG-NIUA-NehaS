# UPYOG UI Modular Build & Deployment Strategy
## Proposal for Independent Module Build and Deployment using Webpack Module Federation

---

**Prepared for:** NUDM UPYOG Team  
**Date:** January 2025  
**Version:** 1.0  
**Prepared By:** Shivank Shukla NUDM

---

## Executive Summary

### Current Challenge

Currently, any change to a single module (e.g., PT, PTR, FSM) requires rebuilding and redeploying the **entire UPYOG UI application** (~10+ modules), resulting in:

- **Build time:** 30-40 minutes for complete build
- **Deployment risk:** All modules affected by single module change
- **Resource waste:** Rebuilding unchanged modules
- **Slower iterations:** Developers wait for full build cycle

### Proposed Solution

Implement **Webpack Module Federation** to enable:

- Independent module builds (5-10 min per module)
- Deploy only changed modules
- Parallel builds in Jenkins
- Faster rollback capabilities
- Reduced deployment risk

### Expected Benefits

| Metric | Current | After Implementation | Improvement |
|--------|---------|---------------------|-------------|
| Build Time (single module) | 30-40 min | 5-10 min | **75% faster** |
| Deployment Time | 1-2 min | 30-55 Sec | **70% faster** |
| Risk on Deployment | High (all modules) | Low (single module) | **Isolated** |

---

## Table of Contents

1. Current Architecture Analysis
2. Proposed Architecture
3. Implementation Approach
4. Step-by-Step Implementation Plan
5. Technical Requirements
6. Timeline & Resources
7. Risk Assessment
8. Success Metrics

---

## 1. Current Architecture Analysis

### 1.1 Existing Structure

```
frontend/upyog-ui/
├── web/
│   ├── micro-ui-internals/
│   │   └── packages/
│   │       └── modules/
│   │           ├── pt/          (Property Tax)
│   │           ├── ptr/         (Pet Registration)
│   │           ├── fsm/         (Sanitation)
│   │           ├── pgr/         (Grievances)
│   │           ├── tl/          (Trade License)
│   │           └── ... (10+ modules)
│   ├── src/
│   │   └── App.js              (Registers ALL modules)
│   └── docker/
│       └── Dockerfile          (Builds everything)
```

### 1.2 Current Build Process Flow

```
Code Change in PT Module
    ↓
Full Build Triggered
    ↓
Build ALL 10+ Modules
    ↓
Create Single Docker Image
    ↓
Deploy Entire Application
    ↓
All Modules Redeployed
```

**Time:** 30-40 minutes  
**Risk:** High (all modules affected)

### 1.3 Problems Identified

1. **Monolithic Build:** All modules bundled in single Docker image
2. **Tight Coupling:** All modules loaded in App.js regardless of usage
3. **No Isolation:** One module's bug can break entire application
4. **Slow CI/CD:** Jenkins builds everything sequentially
5. **Resource Intensive:** Unnecessary rebuilds consume compute resources

---

## 2. Proposed Architecture

### 2.1 Micro-Frontend Architecture with Module Federation

```
┌─────────────────────────────────────────────────────┐
│           Shell Application (Host)                   │
│  - Core routing                                      │
│  - Authentication                                    │
│  - Common components                                 │
└──────────────┬──────────────────────────────────────┘
               │
               │ Dynamically loads modules
               │
    ┌──────────┼──────────┬──────────┬──────────┐
    │          │          │          │          │
┌───▼───┐  ┌──▼───┐  ┌───▼───┐  ┌──▼───┐  ┌───▼───┐
│  PT   │  │ PTR  │  │  FSM  │  │ PGR  │  │  TL   │
│Module │  │Module│  │Module │  │Module│  │Module │
└───────┘  └──────┘  └───────┘  └──────┘  └───────┘
   │          │          │          │          │
Independent Docker Images & Deployments
```

### 2.2 Module Federation Benefits

**Webpack Module Federation** allows:

- Each module is a separate build artifact
- Modules loaded at runtime (not build time)
- Shared dependencies (React, React-DOM) loaded once
- Independent versioning per module
- Hot-swappable modules without full redeployment

---

## 3. Implementation Approach

### 3.1 Architecture Components

#### A. Shell Application (Host)

- Core UPYOG UI framework
- Routing infrastructure
- Authentication & authorization
- Common components (Header, Footer, Navigation)
- Module registry and loader

#### B. Remote Modules

Each module (PT, PTR, FSM, etc.) becomes a "remote":

- Self-contained build
- Exposes components via Module Federation
- Independent deployment
- Own Docker image

#### C. Shared Dependencies

Common libraries shared across modules:

- React, React-DOM
- React Router
- Digit UI Components
- Common utilities

### 3.2 Build & Deployment Flow

```
Developer Changes PT Module
    ↓
Git Push
    ↓
Jenkins Detects Change
    ↓
Build ONLY PT Module
    ↓
Create PT Docker Image
    ↓
Deploy PT Module Only
    ↓
Shell App Loads New PT Version
    ↓
Other Modules Unaffected
```

**Time:** 5-10 minutes  
**Risk:** Low (only PT module affected)

---

## 4. Step-by-Step Implementation Plan

### Phase 1: Foundation Setup (Week 1-2)

#### Step 1.1: Create Shell Application

**Location:** `frontend/upyog-ui/web/shell/`

**Files to create:**

```
shell/
├── src/
│   ├── App.js                 (Module loader)
│   ├── bootstrap.js           (Initialize federation)
│   └── routes.js              (Dynamic routing)
├── webpack.config.js          (Federation config)
├── package.json
└── Dockerfile
```

**Shell webpack.config.js:**

```javascript
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');

module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      name: 'shell',
      remotes: {
        pt_module: 'pt_module@http://upyog-ui-pt/remoteEntry.js',
        ptr_module: 'ptr_module@http://upyog-ui-ptr/remoteEntry.js',
        fsm_module: 'fsm_module@http://upyog-ui-fsm/remoteEntry.js',
      },
      shared: {
        react: { singleton: true, requiredVersion: '^17.0.2' },
        'react-dom': { singleton: true, requiredVersion: '^17.0.2' },
        'react-router-dom': { singleton: true },
      }
    })
  ]
};
```

#### Step 1.2: Configure PT Module as Remote

**Location:** `frontend/upyog-ui/web/micro-ui-internals/packages/modules/pt/`

**Create webpack.config.js:**

```javascript
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');

module.exports = {
  entry: './src/index.js',
  output: {
    publicPath: 'auto',
  },
  plugins: [
    new ModuleFederationPlugin({
      name: 'pt_module',
      filename: 'remoteEntry.js',
      exposes: {
        './Module': './src/Module.js',
        './PTLinks': './src/pages/employee/index.js',
      },
      shared: {
        react: { singleton: true },
        'react-dom': { singleton: true },
        'react-router-dom': { singleton: true },
      }
    })
  ]
};
```

#### Step 1.3: Create Module-Specific Dockerfile

**Location:** `build/frontend/pt-module/Dockerfile`

```dockerfile
FROM node:14-alpine AS build

WORKDIR /app
COPY frontend/upyog-ui/web/micro-ui-internals/packages/modules/pt ./

RUN yarn install
RUN yarn build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html/pt
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
```

#### Step 1.4: Update build-config.yml

**Location:** `build/build-config.yml`

Add entries for each module:

```yaml
# PT Module
- name: builds/upyog/frontend/upyog-ui/pt-module
  build:
    - work-dir: frontend/upyog-ui/web/micro-ui-internals/packages/modules/pt
      dockerfile: build/frontend/pt-module/Dockerfile
      image-name: upyog-ui-pt-module

# PTR Module  
- name: builds/upyog/frontend/upyog-ui/ptr-module
  build:
    - work-dir: frontend/upyog-ui/web/micro-ui-internals/packages/modules/ptr
      dockerfile: build/frontend/ptr-module/Dockerfile
      image-name: upyog-ui-ptr-module

# Shell Application
- name: builds/upyog/frontend/upyog-ui/shell
  build:
    - work-dir: frontend/upyog-ui/web/shell
      dockerfile: build/frontend/shell/Dockerfile
      image-name: upyog-ui-shell
```

---

### Phase 2: Jenkins Pipeline Configuration (Week 2-3)

#### Step 2.1: Create Module Detection Script

**Location:** `frontend/upyog-ui/scripts/detect-changed-modules.sh`

```bash
#!/bin/bash
# Detect which modules changed in git commit

CHANGED_FILES=$(git diff --name-only HEAD~1)
MODULES_TO_BUILD=""

if echo "$CHANGED_FILES" | grep -q "modules/pt/"; then
  MODULES_TO_BUILD="$MODULES_TO_BUILD pt"
fi

if echo "$CHANGED_FILES" | grep -q "modules/ptr/"; then
  MODULES_TO_BUILD="$MODULES_TO_BUILD ptr"
fi

echo $MODULES_TO_BUILD
```

#### Step 2.2: Update Jenkinsfile

**Location:** `frontend/upyog-ui/Jenkinsfile`

```groovy
library 'ci-libs'

pipeline {
  agent any
  
  stages {
    stage('Detect Changes') {
      steps {
        script {
          CHANGED_MODULES = sh(
            script: './scripts/detect-changed-modules.sh',
            returnStdout: true
          ).trim()
          echo "Modules to build: ${CHANGED_MODULES}"
        }
      }
    }
    
    stage('Build Modules') {
      steps {
        script {
          def modules = CHANGED_MODULES.split(' ')
          modules.each { module ->
            buildPipeline(
              configFile: "./build/build-config.yml",
              moduleName: "upyog-ui/${module}-module"
            )
          }
        }
      }
    }
  }
}
```

---

### Phase 3: Helm Chart Updates (Week 3-4)

#### Step 3.1: Create Module-Specific Helm Charts

**Location:** `config-as-code/helm/charts/frontend/`

```
frontend/
├── upyog-ui-shell/
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
├── upyog-ui-pt-module/
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
└── upyog-ui-ptr-module/
    ├── Chart.yaml
    ├── values.yaml
    └── templates/
```

#### Step 3.2: PT Module Helm Values

**Location:** `config-as-code/helm/charts/frontend/upyog-ui-pt-module/values.yaml`

```yaml
namespace: egov

labels:
  app: "upyog-ui-pt-module"
  group: "web"
  module: "pt"

ingress:
  enabled: true
  context: "upyog-ui/pt"

image:
  repository: "upyog-ui-pt-module"
  
replicas: 2

httpPort: 80

healthChecks:
  enabled: true
  livenessProbePath: "/pt/remoteEntry.js"
  readinessProbePath: "/pt/remoteEntry.js"

resources:
  requests:
    memory: "256Mi"
    cpu: "200m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

#### Step 3.3: Shell Application Helm Values

**Location:** `config-as-code/helm/charts/frontend/upyog-ui-shell/values.yaml`

```yaml
namespace: egov

labels:
  app: "upyog-ui-shell"
  group: "web"

ingress:
  enabled: true
  context: "upyog-ui"

image:
  repository: "upyog-ui-shell"
  
replicas: 3

env: |
  - name: PT_MODULE_URL
    value: "http://upyog-ui-pt-module/remoteEntry.js"
  - name: PTR_MODULE_URL
    value: "http://upyog-ui-ptr-module/remoteEntry.js"
  - name: FSM_MODULE_URL
    value: "http://upyog-ui-fsm-module/remoteEntry.js"

resources:
  requests:
    memory: "512Mi"
    cpu: "300m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

---

### Phase 4: Testing & Validation (Week 4-5)

#### Step 4.1: Local Testing

```bash
# Terminal 1: Start PT Module
cd frontend/upyog-ui/web/micro-ui-internals/packages/modules/pt
yarn start

# Terminal 2: Start Shell
cd frontend/upyog-ui/web/shell
yarn start

# Access: http://localhost:3000
```

#### Step 4.2: Integration Testing Checklist

- PT module loads in shell application
- Routing works correctly
- Shared dependencies load once
- Authentication flows work
- API calls function properly
- Module hot-reload works
- Error boundaries catch module failures

#### Step 4.3: Performance Testing

- Initial load time less than 3 seconds
- Module lazy load less than 1 second
- Memory usage acceptable
- No duplicate library loading

---

### Phase 5: Production Rollout (Week 5-6)

#### Step 5.1: Deployment Strategy

**Blue-Green Deployment:**

- Week 1: Deploy to DEV environment
- Week 2: Deploy to QA environment  
- Week 3: Deploy to UAT environment
- Week 4: Deploy to PROD (20% traffic)
- Week 5: Deploy to PROD (100% traffic)

#### Step 5.2: Rollback Plan

If issues occur:

1. Revert to monolithic build (existing setup)
2. Time to rollback: less than 15 minutes
3. Zero data loss
4. Automated rollback triggers

---

## 5. Technical Requirements

### 5.1 Infrastructure Requirements

| Component | Current | Required | Notes |
|-----------|---------|----------|-------|
| Jenkins Agents | 2 | 4 | Parallel builds |
| Docker Registry Storage | 100GB | 200GB | More images |
| Kubernetes Nodes | 3 | 3 | Same |
| Memory per Node | 16GB | 16GB | Same |

### 5.2 Software Requirements

| Software | Version | Purpose |
|----------|---------|---------|
| Node.js | 14.x | Build environment |
| Webpack | 5.x | Module Federation |
| Yarn | 1.22.x | Package manager |
| Docker | 20.x | Containerization |
| Kubernetes | 1.21+ | Orchestration |

### 5.3 Team Requirements

| Role | Time Commitment | Duration |
|------|----------------|----------|
| Frontend Architect | 100% | 6 weeks |
| DevOps Engineer | 100% | 6 weeks |
| Frontend Developer | 50% | 6 weeks |
| QA Engineer | 50% | 3 weeks |

---

## 6. Timeline & Resources

### 6.1 Detailed Timeline

**Phase 1: Foundation (Week 1-2)**
- Shell application setup
- PT module configuration
- Dockerfile creation

**Phase 2: CI/CD (Week 2-3)**
- Jenkins pipeline updates
- Build configuration
- Pipeline testing

**Phase 3: Deployment (Week 3-4)**
- Helm charts creation
- Kubernetes configuration

**Phase 4: Testing (Week 4-5)**
- Integration testing
- Performance testing

**Phase 5: Rollout (Week 5-6)**
- Environment deployments
- Production rollout

**Total Duration:** 6 weeks

### 6.2 Resource Allocation

**Budget Estimate:**

- Infrastructure: Rs. 50,000 (additional storage)
- Training: Rs. 30,000 (team upskilling)
- Contingency: Rs. 20,000
- **Total: Rs. 1,00,000**

---

## 7. Risk Assessment

### 7.1 Identified Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Module compatibility issues | Medium | High | Extensive testing, gradual rollout |
| Performance degradation | Low | Medium | Load testing, monitoring |
| Team learning curve | Medium | Low | Training sessions, documentation |
| Deployment complexity | Medium | Medium | Automation, runbooks |
| Rollback challenges | Low | High | Blue-green deployment, backups |

### 7.2 Mitigation Strategies

**1. Compatibility Issues:**
- Maintain backward compatibility
- Version pinning for shared dependencies
- Comprehensive integration tests

**2. Performance:**
- CDN for module delivery
- Aggressive caching strategies
- Performance monitoring (Lighthouse, Web Vitals)

**3. Team Readiness:**
- 2-day training workshop
- Detailed documentation
- Pair programming sessions

---

## 8. Success Metrics

### 8.1 Key Performance Indicators (KPIs)

| Metric | Current | Target | Measurement |
|--------|---------|--------|-------------|
| Build Time (single module) | 35 min | 8 min | Jenkins logs |
| Deployment Frequency | 2/week | 10/week | Git commits |
| Mean Time to Recovery | 45 min | 10 min | Incident logs |
| Failed Deployment Rate | 15% | 5% | Jenkins stats |
| Developer Satisfaction | 6/10 | 9/10 | Survey |

### 8.2 Success Criteria

**Must Have (Go-Live Criteria):**

- All modules build independently
- Build time less than 10 min per module
- Zero downtime deployments
- Rollback time less than 15 min
- All tests passing

**Nice to Have:**

- Automated module versioning
- A/B testing capability
- Module analytics dashboard
- Auto-scaling per module

---

## 9. Conclusion & Recommendations

### 9.1 Summary

The proposed Webpack Module Federation approach will transform UPYOG UI from a monolithic application to a scalable micro-frontend architecture, enabling:

- **75% faster builds** for individual modules
- **Independent deployments** reducing risk
- **Better developer experience** with faster iterations
- **Improved scalability** for future growth

### 9.2 Recommendations

1. **Approve Phase 1** implementation immediately
2. **Allocate dedicated team** for 6-week period
3. **Start with 3 pilot modules** (PT, PTR, FSM)
4. **Expand to all modules** after successful pilot
5. **Invest in monitoring** and observability tools

### 9.3 Next Steps

1. **Week 1:** Stakeholder approval & team allocation
2. **Week 2:** Begin Phase 1 implementation
3. **Week 4:** First demo to stakeholders
4. **Week 6:** DEV environment deployment
5. **Week 8:** Production rollout planning

---

## Appendix

### A. Glossary

- **Module Federation:** Webpack 5 feature for runtime code sharing
- **Micro-Frontend:** Architectural pattern for frontend modularity
- **Remote:** Independently deployed module
- **Host/Shell:** Main application loading remotes
- **Blue-Green Deployment:** Zero-downtime deployment strategy

### B. References

1. Webpack Module Federation Documentation: https://webpack.js.org/concepts/module-federation/
2. Micro-Frontend Architecture: https://martinfowler.com/articles/micro-frontends.html
3. UPYOG Documentation: https://upyog-docs.gitbook.io/upyog-v-1.0/

### C. Current UPYOG UI Modules

**Total Modules: 30+**

- PT (Property Tax)
- PTR (Pet Registration)
- FSM (Faecal Sludge Management)
- PGR (Public Grievance Redressal)
- TL (Trade License)
- WS (Water & Sewerage)
- OBPS (Online Building Plan Approval)
- NOC (No Objection Certificate)
- HRMS (Human Resource Management)
- MCollect (Miscellaneous Collection)
- DSS (Decision Support System)
- Engagement (Citizen Engagement)
- Bills, Receipts, Reports
- ADS (Advertisement)
- ASSET (Asset Management)
- CHB (Community Hall Booking)
- EW (E-Waste)
- SV (Street Vending)
- WT (Water Tanker)
- VENDOR (Vendor Management)
- PGRAI (PGR AI Services)
- EST (Estate Management)
- GIS (Geographic Information System)

---

**Document Version:** 1.0  
**Last Updated:** January 2025  
**Status:** Pending Approval  
**Prepared for:** NIUA UPYOG Team
