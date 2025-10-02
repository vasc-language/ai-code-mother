declare namespace API {
  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVersionQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    appId?: number
    versionNum?: number
    versionTag?: string
    userId?: number
  }

  type AppVersionRollbackRequest = {
    appId?: number
    versionId?: number
  }

  type AppVersionVO = {
    id?: number
    appId?: number
    versionNum?: number
    versionTag?: string
    codeContent?: string
    deployKey?: string
    deployUrl?: string
    deployedTime?: string
    userId?: number
    remark?: string
    createTime?: string
    user?: UserVO
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type BaseResponseAppVersionVO = {
    code?: number
    data?: AppVersionVO
    message?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListAppVersionVO = {
    code?: number
    data?: AppVersionVO[]
    message?: string
  }

  type BaseResponseListInviteRecord = {
    code?: number
    data?: InviteRecord[]
    message?: string
  }

  type BaseResponseListPointsRecord = {
    code?: number
    data?: PointsRecord[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseMapStringInteger = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponseMapStringObject = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponseMapStringString = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponsePageAppVersionVO = {
    code?: number
    data?: PageAppVersionVO
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUserProfileVO = {
    code?: number
    data?: UserProfileVO
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type chatToGenCodeParams = {
    appId: number
    message: string
    runId?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type DiagramTask = {
    mermaidCode?: string
    description?: string
  }

  type downloadAppCodeParams = {
    appId: number
  }

  type downloadCodeFilesParams = {
    appId: number
  }

  type executeWorkflowParams = {
    prompt: string
  }

  type executeWorkflowWithFluxParams = {
    prompt: string
  }

  type executeWorkflowWithSseParams = {
    prompt: string
  }

  type getAppVOByIdByAdminParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getFileContentParams = {
    appId: number
    filePath: string
  }

  type getPointsRecordsParams = {
    /** 积分类型（SIGN_IN/REGISTER/INVITE/GENERATE/FIRST_GENERATE） */
    type?: string
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserProfileParams = {
    userId: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type getVersionVOParams = {
    versionId: number
  }

  type grantPointsForAdminsParams = {
    points?: number
  }

  type IllustrationTask = {
    query?: string
  }

  type ImageCollectionPlan = {
    contentImageTasks?: ImageSearchTask[]
    illustrationTasks?: IllustrationTask[]
    diagramTasks?: DiagramTask[]
    logoTasks?: LogoTask[]
  }

  type ImageResource = {
    category?: 'CONTENT' | 'LOGO' | 'ILLUSTRATION' | 'ARCHITECTURE'
    description?: string
    url?: string
  }

  type ImageSearchTask = {
    query?: string
  }

  type initializePointsForUserParams = {
    userId: number
    points?: number
  }

  type InviteRecord = {
    id?: number
    inviterId?: number
    inviteeId?: number
    inviteCode?: string
    registerIp?: string
    deviceId?: string
    status?: string
    inviterPoints?: number
    inviteePoints?: number
    createTime?: string
    registerTime?: string
    rewardTime?: string
    isDelete?: number
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type listCodeFilesParams = {
    appId: number
  }

  type listVersionsByAppIdParams = {
    appId: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type LogoTask = {
    description?: string
  }

  type PageAppVersionVO = {
    records?: AppVersionVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PointsRecord = {
    id?: number
    userId?: number
    points?: number
    balance?: number
    type?: string
    reason?: string
    relatedId?: number
    expireTime?: string
    createTime?: string
    isDelete?: number
  }

  type QualityResult = {
    isValid?: boolean
    errors?: string[]
    suggestions?: string[]
  }

  type SendEmailCodeRequest = {
    email?: string
    type?: string
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type SseEmitter = {
    timeout?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserAppsQueryRequest = {
    userId?: number
    pageNum?: number
    pageSize?: number
    sortBy?: string
    sortOrder?: string
    genType?: string
  }

  type UserLoginRequest = {
    userEmail?: string
    userPassword?: string
  }

  type UserProfileUpdateRequest = {
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserProfileVO = {
    user?: UserVO
    statistics?: UserStatisticsVO
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userEmail?: string
    emailCode?: string
    userPassword?: string
    checkPassword?: string
    inviteCode?: string
  }

  type UserStatisticsVO = {
    appCount?: number
    generateCount?: number
    joinDays?: number
    lastActiveTime?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }

  type WorkflowContext = {
    currentStep?: string
    originalPrompt?: string
    imageListStr?: string
    imageList?: ImageResource[]
    imageCollectionPlan?: ImageCollectionPlan
    contentImages?: ImageResource[]
    illustrations?: ImageResource[]
    diagrams?: ImageResource[]
    logos?: ImageResource[]
    enhancedPrompt?: string
    generationType?: 'HTML' | 'MULTI_FILE' | 'VUE_PROJECT'
    generatedCodeDir?: string
    buildResultDir?: string
    qualityResult?: QualityResult
    errorMessage?: string
  }
}
